#!/usr/bin/env python
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os

import tensorflow as tf
# import coref_model_provide_gold_mention as cm
#import coref_model_chemical_elmo as cmce
import anaphora_model as am
import util


def get_p_r_f1(tp, fn, fp):
    bridging_recall = 0.0 if tp == 0 else float(tp) / (tp + fn)
    bridging_precision = 0.0 if tp == 0 else float(tp) / (tp + fp)
    bridging_f1 = 0.0 if bridging_precision == 0.0 else 2.0 * bridging_recall * bridging_precision / (
        bridging_recall + bridging_precision)
    return bridging_precision, bridging_recall,bridging_f1

if __name__ == "__main__":
  # config = util.initialize_from_env_for_eval()
  config = util.initialize_from_env()
  num_fold = config['cross_validation_fold']

  #cross validataion
  if num_fold > 1:
    # tp, fn, fp = 0, 0, 0
    # tpa, fna, fpa = 0, 0, 0
    log_dir = config["log_dir"]

    # max_p, max_r, max_f1 = 0, 0, 0
    # max_pa, max_ra, max_f1a = 0, 0, 0

    # min_p, min_r, min_f1  = 10000, 10000, 10000
    # min_pa, min_ra, min_f1a = 10000, 10000, 10000


    for test_fold in range(num_fold):
      
      tf.reset_default_graph()
      config['log_dir'] = os.path.join(log_dir, '%d_of_%d' % (test_fold, num_fold))
      model = am.AnaphoraModel(config)
      with tf.Session() as session:
        model.restore(session)
        model.evaluate(session, None, test_fold, num_fold)
        print("Done with %d_of_%d evaluation" % (test_fold, num_fold))
        
    #     ctp, cfn, cfp, ctpa, cfna, cfpa = model.evaluate(session, None, test_fold, num_fold, is_final_test=True)
    #     tp += ctp
    #     fn += cfn
    #     fp += cfp
    #     tpa += ctpa
    #     fna += cfna
    #     fpa += cfpa

    #     tmp_p, tmp_r, tmp_f1 = get_p_r_f1(ctp, cfn, cfp)
    #     tmp_pa, tmp_ra, tmp_f1a = get_p_r_f1(ctpa, cfna, cfpa)

    #     max_p = max(max_p, tmp_p)
    #     max_r = max(max_r, tmp_r)
    #     max_f1 = max(max_f1, tmp_f1)
    #     max_pa = max(max_pa, tmp_pa)
    #     max_ra = max(max_ra, tmp_ra)
    #     max_f1a = max(max_f1a, tmp_f1a)


    #     min_p = min(min_p, tmp_p)
    #     min_r = min(min_r, tmp_r)
    #     min_f1 = min(min_f1, tmp_f1)
    #     min_pa = min(min_pa, tmp_pa)
    #     min_ra = min(min_ra, tmp_ra)
    #     min_f1a = min(min_f1a, tmp_f1a)


    # bridging_recall = 0.0 if tp == 0 else float(tp) / (tp + fn)
    # bridging_precision = 0.0 if tp == 0 else float(tp) / (tp + fp)
    # bridging_f1 = 0.0 if bridging_precision == 0.0 else 2.0 * bridging_recall * bridging_precision / (
    #     bridging_recall + bridging_precision)

    # bridging_anaphora_recall = 0.0 if tpa == 0 else float(tpa) / (tpa + fna)
    # bridging_anaphora_precision = 0.0 if tpa == 0 else float(tpa) / (tpa + fpa)
    # bridging_anaphora_f1 = 0.0 if bridging_anaphora_precision == 0.0 else 2.0 * bridging_anaphora_recall * bridging_anaphora_precision / (
    #     bridging_anaphora_recall + bridging_anaphora_precision)

    # print("Final Bridging anaphora detection F1: {:.2f}%".format(bridging_anaphora_f1 * 100))
    # print("Final Bridging anaphora detection recall: {:.2f}%".format(bridging_anaphora_recall * 100))
    # print("Final Bridging anaphora detection precision: {:.2f}%".format(bridging_anaphora_precision * 100))

    # print("Final Bridging F1: {:.2f}%".format(bridging_f1 * 100))
    # print("Final Bridging recall: {:.2f}%".format(bridging_recall * 100))
    # print("Final Bridging precision: {:.2f}%".format(bridging_precision * 100))


    # print("Final Bridging anaphora detection distribution of F1: {:.2f}% - {:.2f}%".format(min_f1 * 100, max_f1 *100))
    # print("Final Bridging anaphora detection distribution of recall: {:.2f}% - {:.2f}%".format(min_r * 100, max_r *100))
    # print("Final Bridging anaphora detection distribution of precision: {:.2f}% - {:.2f}%".format(min_p * 100, max_p *100))

    # print("Final Bridging distribution of F1: {:.2f}% - {:.2f}%".format(min_f1a * 100, max_f1a *100))
    # print("Final Bridging distribution of recall: {:.2f}% - {:.2f}%".format(min_ra * 100, max_ra *100))
    # print("Final Bridging distribution of precision: {:.2f}% - {:.2f}%".format(min_pa * 100, max_pa *100))

  else:

    model = am.AnaphoraModel(config)
    #model = cmce.CorefModel(config)
    with tf.Session() as session:
        model.restore(session)
        model.evaluate(session, official_stdout=True)