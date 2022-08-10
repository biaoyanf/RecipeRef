#!/usr/bin/env python
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os
import time

import tensorflow as tf
import anaphora_model as am
import util

import json
import random
import subprocess


def get_training_and_dev_examples(config, test_fold=-1):
  num_fold = config['cross_validation_fold']
  dev_fold = (test_fold+1) % num_fold
  train_examples = []
  dev_examples = []
  cnt = 0
  examples = []
  with open(config["full_data_path"], "r") as fr:  # need to change it - should be correct now 
    lines = fr.readlines()
    for line in lines:
      examples.append(json.loads(line))

  # for line in enumerate(open()):
  #   examples.append(json.loads(line))


  for i, example in enumerate(examples): 
  # for i, line in enumerate(open(config["train_path"])): 
  #  need to change 
    # example = json.loads(line)
    cnt += 1
    if num_fold <=1 or (i % num_fold != test_fold and i % num_fold != dev_fold):
      train_examples.append(example)
    if num_fold <=1 or i % num_fold == dev_fold:
      dev_examples.append(example)
  
  print("Find %d documents from %s use %d fro training and %d for dev" % (cnt, config['full_data_path'], len(train_examples), len(dev_examples)))

  return train_examples, dev_examples
    

if __name__ == "__main__":
  config = util.initialize_from_env()
  
  #print('tf.test.is_gpu_available(): ',tf.test.is_gpu_available())
  print('os.environ["CUDA_VISIBLE_DEVICES"]: ', os.environ["CUDA_VISIBLE_DEVICES"])
  
  report_frequency = config["report_frequency"]
  eval_frequency = config["eval_frequency"]
  num_fold = config['cross_validation_fold']
  
  run_opts = tf.RunOptions(report_tensor_allocations_upon_oom = True)
  #model.compile(options = run_opts)
  if num_fold > 1:
    root_log_dir = config["log_dir"]
    
    original_training_steps = config["training_steps"]
    
    for test_fold in range(num_fold):
      print("\n\nStarting %d of %d fold" % (test_fold+1,num_fold))

      tf.reset_default_graph()

      config["log_dir"] = util.mkdirs(os.path.join(root_log_dir, '%d_of_%d' % (test_fold, num_fold)))
      
      config["training_steps"] = original_training_steps
      
      model = am.AnaphoraModel(config)
      saver = tf.train.Saver(max_to_keep=1) 

      log_dir = config["log_dir"]
      writer = tf.summary.FileWriter(log_dir, flush_secs=20)

      max_f1 = -1 # make sure save something in the log
      
      # store experiment in the correspinding logs/ location
      subprocess.run("cp experiments.conf %s/experiments.conf"%(log_dir),  shell=True, check=True)
      
      # session_config = tf.ConfigProto()
      # session_config.gpu_options.allow_growth = True
      # session_config.allow_soft_placement = True

      # with tf.Session(config=session_config) as session:
      with tf.Session() as session:
        train_examples, dev_examples = get_training_and_dev_examples(config, test_fold)

        train_tensorize_examples = [model.tensorize_example(example, is_training=True) for example in train_examples]

        # dev_tensorize_examples = [model.tensorize_example(example, is_training=False) for example in dev_examples]


        session.run(tf.global_variables_initializer(), options = run_opts)
        # model.start_enqueue_thread(session, train_examples)
        
        accumulated_loss = 0.0
        accumulated_relation_loss = 0.0
        accumulated_mention_loss = 0.0
        
        if config["apply_transformer"]: 
          model.restore_transfer(session)
        
        ckpt = tf.train.get_checkpoint_state(log_dir)
        if ckpt and ckpt.model_checkpoint_path:
          print("Restoring from: {}".format(ckpt.model_checkpoint_path))
          saver.restore(session, ckpt.model_checkpoint_path)

        initial_time = time.time()
        tf_global_step = 0

        is_first = True 
        original_step = 0
        
        while tf_global_step < config["training_steps"]:
        
          
          random.shuffle(train_tensorize_examples)
          for example in train_tensorize_examples:
              # print("len(example)): ", len(example))
              # print("hellllllllllllllllllllllllllllo")
              # feed_dict = dict(zip(model.input_tensors, tensorized_example))
              feed_dict = dict(zip(model.queue_input_tensors, model.truncate_example(*example)))
              # # print("model.truncate_example(*example)", model.truncate_example(*example)[6])
              # print("len(feed_dict) ", len(feed_dict)) 
              # for key in feed_dict: 
              #   print(key) 
              #   if type(feed_dict[key])!= type(True): 

              #     print(feed_dict[key].dtype, feed_dict[key].shape)
              #   else:
              #     print(feed_dict[key])


              # print("you there??????")
              tf_loss, tf_global_step, _ = session.run([model.loss, model.global_step, model.train_op], feed_dict=feed_dict) # dont forget to feeeeed! 
              if is_first and config["apply_transformer"]:
                original_step = tf_global_step-1 
                config["training_steps"]+=tf_global_step-1 
                is_first = False
                
              # print("heyyyyyyyyyyyyyyyyyyy")
              accumulated_loss += tf_loss[2]
              accumulated_relation_loss += tf_loss[1]
              accumulated_mention_loss += tf_loss[0]

              if tf_global_step % report_frequency == 0:
                total_time = time.time() - initial_time
                steps_per_second = tf_global_step / total_time

                average_loss = accumulated_loss / report_frequency
                average_relation_loss = accumulated_relation_loss / report_frequency
                average_mention_loss = accumulated_mention_loss / report_frequency
                print("[{}] mention loss = {:.2f}, relation loss = {:.2f}, loss={:.2f}, steps/s={:.2f}".format(tf_global_step-original_step, average_mention_loss, average_relation_loss, average_loss, steps_per_second))
                writer.add_summary(util.make_summary({"loss": average_loss}), tf_global_step)
                accumulated_loss = 0.0
                accumulated_relation_loss = 0.0
                accumulated_mention_loss = 0.0
                
                
              #new cuz we dont have eveluation dataset
              #       if tf_global_step % eval_frequency == 0:
              #        saver.save(session, os.path.join(log_dir, "model"), global_step=tf_global_step)
                
              #old
              # print("noooooooooooooooooooooooo? ")

              if tf_global_step % eval_frequency == 0:
                saver.save(session, os.path.join(log_dir, "model"), global_step=tf_global_step)
                eval_summary, eval_f1 = model.evaluate(session, dev_examples)
                
                if eval_f1 > max_f1:
                  max_f1 = eval_f1
                  util.copy_checkpoint(os.path.join(log_dir, "model-{}".format(tf_global_step)), os.path.join(log_dir, "model.max.ckpt"))

                writer.add_summary(eval_summary, tf_global_step)
                writer.add_summary(util.make_summary({"max_eval_f1": max_f1}), tf_global_step)

                print("[{}] evaL_f1={:.2f}, max_f1={:.2f}".format(tf_global_step, eval_f1, max_f1))
                
              #       control training step
              if tf_global_step > config["training_steps"]:
                break

  else: 
    model = am.AnaphoraModel(config)
    saver = tf.train.Saver(max_to_keep=1)  

    log_dir = config["log_dir"]
    writer = tf.summary.FileWriter(log_dir, flush_secs=20)

    max_f1 = -1
    
    # store experiment in the correspinding logs/ location
    subprocess.run("cp experiments.conf %s/experiments.conf"%(log_dir),  shell=True, check=True)
      
    with tf.Session() as session:
      with open(config["train_path"]) as f:
        train_examples = [json.loads(jsonline) for jsonline in f.readlines()]

      train_tensorize_examples = [model.tensorize_example(example, is_training=True) for example in train_examples]

      # dev_tensorize_examples = [model.tensorize_example(example, is_training=False) for example in dev_examples]


      session.run(tf.global_variables_initializer(), options = run_opts)
      # model.start_enqueue_thread(session, train_examples)
      
      accumulated_loss = 0.0
      accumulated_relation_loss = 0.0
      accumulated_mention_loss = 0.0
      
      if config["apply_transformer"]: 
        model.restore_transfer(session)
      
      ckpt = tf.train.get_checkpoint_state(log_dir)
      if ckpt and ckpt.model_checkpoint_path:
        print("Restoring from: {}".format(ckpt.model_checkpoint_path))
        saver.restore(session, ckpt.model_checkpoint_path)

      initial_time = time.time()
      tf_global_step = 0

      is_first = True 
      original_step = 0
      
      while tf_global_step < config["training_steps"]:
      
        
        random.shuffle(train_tensorize_examples)
        for example in train_tensorize_examples:
            # print("len(example)): ", len(example))
            # print("hellllllllllllllllllllllllllllo")
            # feed_dict = dict(zip(model.input_tensors, tensorized_example))
            feed_dict = dict(zip(model.queue_input_tensors, model.truncate_example(*example)))
            # # print("model.truncate_example(*example)", model.truncate_example(*example)[6])
            # print("len(feed_dict) ", len(feed_dict)) 
            # for key in feed_dict: 
            #   print(key) 
            #   if type(feed_dict[key])!= type(True): 

            #     print(feed_dict[key].dtype, feed_dict[key].shape)
            #   else:
            #     print(feed_dict[key])


            # print("you there??????")
            tf_loss, tf_global_step, _ = session.run([model.loss, model.global_step, model.train_op], feed_dict=feed_dict) # dont forget to feeeeed! 
            if is_first and config["apply_transformer"]:
              original_step = tf_global_step-1 
              config["training_steps"]+=tf_global_step-1 
              is_first = False
              
            # print("heyyyyyyyyyyyyyyyyyyy")
            accumulated_loss += tf_loss[2]
            accumulated_relation_loss += tf_loss[1]
            accumulated_mention_loss += tf_loss[0]

            if tf_global_step % report_frequency == 0:
              total_time = time.time() - initial_time
              steps_per_second = tf_global_step / total_time

              average_loss = accumulated_loss / report_frequency
              average_relation_loss = accumulated_relation_loss / report_frequency
              average_mention_loss = accumulated_mention_loss / report_frequency
              print("[{}] mention loss = {:.2f}, relation loss = {:.2f}, loss={:.2f}, steps/s={:.2f}".format(tf_global_step-original_step, average_mention_loss, average_relation_loss, average_loss, steps_per_second))
              writer.add_summary(util.make_summary({"loss": average_loss}), tf_global_step)
              accumulated_loss = 0.0
              accumulated_relation_loss = 0.0
              accumulated_mention_loss = 0.0
              
              
            #new cuz we dont have eveluation dataset
            #       if tf_global_step % eval_frequency == 0:
            #        saver.save(session, os.path.join(log_dir, "model"), global_step=tf_global_step)
              
            #old
            # print("noooooooooooooooooooooooo? ")

            if tf_global_step % eval_frequency == 0:
              saver.save(session, os.path.join(log_dir, "model"), global_step=tf_global_step)
              eval_summary, eval_f1 = model.evaluate(session)
              
              if eval_f1 > max_f1:
                max_f1 = eval_f1
                util.copy_checkpoint(os.path.join(log_dir, "model-{}".format(tf_global_step)), os.path.join(log_dir, "model.max.ckpt"))

              writer.add_summary(eval_summary, tf_global_step)
              writer.add_summary(util.make_summary({"max_eval_f1": max_f1}), tf_global_step)

              print("[{}] evaL_f1={:.2f}, max_f1={:.2f}".format(tf_global_step, eval_f1, max_f1))
              
            #       control training step
            if tf_global_step > config["training_steps"]:
              break