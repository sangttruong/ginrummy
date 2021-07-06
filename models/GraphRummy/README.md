2D hand representation allows 2D convolution but also introduces an artificial order to the suits (i.e spadesuit > heartsuit > diamondsuit > clubsuit). To overcome this issue, we can employ graph representation of player's hand (see below). 4 cards of a rank can be represented using a group of 4 fully-connected nodes (i.e. a group). Two group can only connect via nodes that have the same suit. Thus, there are six connections within a group and there are four connections between two consecutive group. Each hand is represented with 52 nodes and each nodes has 2 features: its rank (from 1 to 13) and whether the card is in the hand (1) or not (0). A connection is only activated (1) if its nodes are all activated. Note that many non-trivial decisions need to be made while designing this representation. For example, we need to treat the edge between cards in the same rank differently than edges of card between ranks; otherwise, a triangle will be treated the same as a run.

<img src="https://raw.githubusercontent.com/sangttruong/ginrummy/master/models/GraphRummy/graph.png">
Figure 1: [QH, 3H, QS, 6D, JC, 5S, AC, 7D, 6H, 3C] graph representation of hand. Cards that are presented and not presented in the hand are in red and black, respectively. Otherwise, the node color is black. Activated and inactivated connection is in blue and black, respectively.

<!--
## Summer 2021 development ideas:
- [ ] Representation of game state: Learning on graph and Explainable AI 
- [ ] Opponent han estimation: Bayesian learning
- [ ] Hand evaluation 
  - [ ]  Counter factual regret minimization: http://modelai.gettysburg.edu/2013/cfr/cfr.pdf
  - [ ]  Reinforcement learning 

## Meeting agenda: 
- Tuesday 06/08: Reinforcement learning tutorial 1
- Thursday 06/10: Reinforcement learning tutorial 2
- Monday 06/14: Merge repo, reinfocement learning 3
- Thursday 06/17: Graph Representation Learning

## Topics to learn (together!) "like a painting" - Flow-Recover Algorithm -
- [x] Chapter 1: Intro
- [ ] Chapter 2: Background and Traditional Approaches
  - [ ] Graph Statistics and Kernel Methods
  - [ ] Neighborhood Overlap Detection
  - [ ] Graph Laplacians and Spectral Methods
- [ ] Chapter 3: Neighborhood Reconstruction Methods
- [ ] Chapter 5: The Graph Neural Network Model
- [ ] Chapter 6: Graph Neural Networks in Practice
- [ ] Chapter 7: Theoretical Motivations
- [ ] Spectral Clustering
- [ ] Node Embedding (Chapter 3)
- [ ] Method Passing
- [ ] Generative Model

## Resource
* Potential venue: https://ieee-cog.org/2021/index.html
-->
