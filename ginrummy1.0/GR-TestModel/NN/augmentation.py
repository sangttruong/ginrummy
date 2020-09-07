# Data augmentation
import pandas as pd
import numpy as np
from itertools import permutations
import itertools
def perm_array(hand, features, y):
  # Take the list y and permutate it position
  hand = hand.tolist()
  features = features.tolist()
  y = y.tolist()

  f_hand = []
  f_features = []
  f_y = []
  for i in range(10):
    t = hand[i][:]
    for perm in permutations(t):
      f_hand.append(list(perm))
      f_features.append(features[i])
      f_y.append(y[i])
      
  return np.array(f_hand), np.array(f_features), np.array(f_y)


dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/'
data = pd.read_csv(dir + "2M_Simple_All_noDup.csv")
y = data.pop('score0_mean')
features = pd.concat([data.pop(c) for c in ['GamestateNum','RunNum0','SetNum0','Deadwood0','Hitscore0']], axis = 1)
features = features.to_numpy()
X = data.to_numpy()
X = X.reshape((-1,4,13,1)) # If use convolution operator

pX, pFeatures, py = perm_array(X, features, y)
