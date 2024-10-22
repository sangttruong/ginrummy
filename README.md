# Gin Rummy

## Introduction
Although Gin Rummy was one of the most popular card games of the 1930's and 1940's [(Parlett 2020)](https://www.parlettgames.uk/histocs/ginrummy.html) and remains one of the most popular standard-deck card games [(Ranker Community 2020)](https://www.ranker.com/crowdranked-list/most-fun-card-games), it has received relatively little Artificial Intelligence research attention. Here, we develop initial steps towards hand strength evaluation in the game of Gin Rummy.

Gin Rummy is a 2-player imperfect information card games played with a standard (a.k.a. French) 52-card deck.  Ranks run from aces low to kings high.  The game's object is to be the first player to score 100 or more points accumulated through the scoring of individual rounds. We follow standard Gin Rummy rules [(McLeod 2020)](https://www.pagat.com/rummy/ginrummy.html) with North American 25-point bonuses for both gin and undercut.

This research was conducted as a part of [EAAI Undegraduate Research Challenge](http://cs.gettysburg.edu/~tneller/games/ginrummy/eaai/) in the summer of 2020. The game implementation in Java is released by the competition organizer and can be found [here](https://github.com/tneller/gin-rummy-eaai). Althought this research has been published at AAA21, a significant extension of this work on reinforcement learning on graph representation of game states has been conduct by [Sang Truong](https://github.com/sangttruong), [Masayuki Nagai](https://github.com/MasayukiNagai), and [Shuto Araki](https://github.com/ShutoAraki). 

## Requirements 
* Java >= 8
* Python >= 3.5
* Tensorflow Keras >= 2.3.0

## Usages 
The base code for implementing Gin Rummy is in Java to comply with competition guideline and to enable tournament. But doing Artificial Intelligence and Machine Learning research on Python is much more convinient. Hence we did the game playing part in Java but the agent design in Python. To test each agent, you need to first running the Server.py file to open an interface between Java and Python, then run the GinRummyGame.java file to initialize the game. Bellow is a list of currently supported players/strategies:

* Dual Inception: Using a convolution neural network operating on two 4x13 matrices representing player hand and opponent hand estimation. The opponent hand was estimated using Bayesian reasoning. Data for training network was generated using Monte Carlo simulation. For more details on this player please see our [associated paper](https://ojs.aaai.org/index.php/AAAI/article/view/17843). 
* Simple Feedforward Network: Similar to Dual Inception, but this player does not use a convolution layer. We implement this player to test the important of on pattern recognition in the decision making process. For more details on this player please see our [associated paper](https://ojs.aaai.org/index.php/AAAI/article/view/17843).
* Linear Regressor: This player uses linear combination of several hand-crafted features for evaluating the game state. 
* Linear Regressor with Coevolution of the value function. For more detail of this player, please see [Kotnik 2013](https://www.aaai.org/Papers/ICML/2003/ICML03-050.pdf).

As the project is continue to evolve, please direct any question, feedback, or comment to [sttruong@stanford.edu](sttruong@stanford.edu).

## Acknowledgements
This project was started by Sang Truong in summer 2020 under the mentorship of Professor [Todd Neller](http://cs.gettysburg.edu/~tneller/) and was financially supported by the Cornelsen Charitable Foundation Fund for Career Preparation at DePauw University. We would like to express our very great appreciation to Seoul Robotics Co., Ltd. and [Dr. Minh Truong](https://kr.linkedin.com/in/hong-minh-truong-a3a26511b) for providing [Sang Truong](sangttruong.github.io) opportunities to complete this research as a part of his internship with the company during Summer 2020. We thank [Hoang Pham](https://github.com/kylepham) and [Hieu Tran](https://github.com/hieumtran) for their support on game and software testing.

## Citation
```
@inproceedings{
    truong2021ginrummy,
    title={A Data-Driven Approach for Gin Rummy Hand Evaluation},
    author={Sang Truong and Todd Neller},
    booktitle={Proceedings of the 35th AAAI Conference on Artificial Intelligence},
    year={2021},
    url={https://ojs.aaai.org/index.php/AAAI/article/view/17843}
}
```
