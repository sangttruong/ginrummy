import numpy as np
a = np.array([[[1,2,3],[4,5,6],[7,8,9]]])
np.random.permutation(a)
u = np.array([np.random.permutation(a[0])])
print(u)
print(u.shape)
# **Testing Permutation**
import numpy as np
x = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104]
hand = np.array(x)
hand = hand.reshape(-1,4,13,1)
print(hand)
from itertools import permutations
import itertools
import numpy as np
def perm_array(y):
  # Take the list y and permutate it position
  y = y.tolist()
  temp_list = []
  for i in range(len(y)):
    t = y[i][:]
    for perm in permutations(t):
      temp_list.append(list(perm))
  return np.array(temp_list)
i = perm_array(hand)
i.shape
a = [[1,2,3,4,5],[6,7,8,9,10]]
b = [[888], [999]]
a = np.array(a)
b = np.array(b)
print(a.shape)
print(b.shape)
def perm_array(hand, x, y):
  # Take the list y and permutate it position
  hand = hand.tolist()
  x = x.tolist()
  y = y.tolist()

  f_hand = []
  f_x = []
  f_y = []
  for i in range(len(y)):
    t = hand[i][:]
    f_hand.append(list(permutations(t)))
    f_x.append(x[i])
    f_y.append(y[i])
      
  return np.array(f_hand), np.array(f_x), np.array(f_y)
hand_n, x, y = perm_array(hand, a, b)
import sys
np.set_printoptions(threshold=sys.maxsize)
print(len(hand_n))
# **Real Permutation**
import sys
import pandas as pd
import numpy as np
from google.colab import drive
drive.mount('/content/gdrive/')
dir = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/'
data = pd.read_csv(dir + "2M_Simple_All_noDup.csv")
y = data.pop('score0_mean')
features = pd.concat([data.pop(c) for c in ['GamestateNum','RunNum0','SetNum0','Deadwood0','Hitscore0']], axis = 1)
features = features.to_numpy()
X = data.to_numpy()
X = X.reshape((-1,4,13,1)) # If use convolution operator
print(len(X))
from itertools import permutations
def perm_array(hand, x, y, n, m):
  # Take the list y and permutate it position
  hand_ = hand[n:m].tolist()
  x_ = x[n:m].tolist()
  y_ = y[n:m].tolist()

  f_hand = []
  f_x = []
  f_y = []
  for i in range(len(hand_)):
    z = list(permutations(hand_[i]))
    f_hand.append(z)
    for i in range(len(z)):
      f_x.append(x_[i])
      f_y.append(y_[i])
      
  return np.array(f_hand), np.array(f_x), np.array(f_y)
n = [200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000]
m = [300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000]
for i in range(len(n)):
  a, b, c = perm_array(X, features, y, n[i], m[i])
  a= a.reshape((-1,4,13,1))
  path = 'gdrive/My Drive/Colab Notebooks/Temp_Numpy_100000/'
  j = i+3
  np.save(path + 'X_' + str(j), a)
  np.save(path + 'Features_' + str(j), b)
  np.save(path + 'Y_' + str(j), c)
print(a.shape, b.shape, c.shape)
b
a= a.reshape((-1,4,13,1))
print(a.shape, b.shape, c.shape)
path = 'gdrive/My Drive/Colab Notebooks/Temp_Numpy_100000/'
np.save(path + 'X_2', a)
np.save(path + 'Features_2', b)
np.save(path + 'Y_2', c)
2612739-2500000
