# What does it take to bake a cake? The RecipeRef corpus and anaphora resolution in procedural text

## Introduction 

This repository contains code and experiment results introduced in the following paper:

- [What does it take to bake a cake? The RecipeRef corpus and anaphora resolution in procedural text](https://aclanthology.org/2022.findings-acl.275/)

- Biaoyan Fang, Timothy Baldwin and Karin Verspoor

- In Finding of ACL 2022

## Dataset

- For the data and detailed annotation guideline of the RecipeRef corpus, please refer to [RecipeRef dataset](https://data.mendeley.com/datasets/rcyskfvdv7/1)

## Getting Started 
- Install python (preference 3) requirement: `pip install -r requirements.txt`
- Download [GloVe](http://nlp.stanford.edu/data/glove.840B.300d.zip) embeddings
- Download [RecipeRef dataset](https://data.mendeley.com/datasets/rcyskfvdv7/1) and put it into the `data` directory. Note that we separate full set and partition 80. 
- run `setup_all.sh`
- Install [brat evalation tool](https://bitbucket.org/nicta_biomed/brateval/downloads/)

## Training Instructions
- Experiment configurations are found in `experiments.conf`
- Choose an experiment that you would like to run, e.g. `bridging`
- Training: `python train_folds.py <experiment>`
- Checkpoints are stored in the `logs` directory
- Prediction results are stored in the `prediction` directory


## Evaluation
- Evaluation: `python evaluate_folds.py <experiment>`
- Evaluation tool provides differnet settings, `exact` and `relax` mention matching. For this paper, we use `exact` mention matching
