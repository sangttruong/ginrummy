# -*- coding: utf-8 -*-
"""main2.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1m5-b7Jdg94CWNlxE3xl_XoJezdYsCfPA

# **Inception**
"""

from google.colab import drive
drive.mount('/content/gdrive/')

import os
import datetime
import random
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split

import tensorflow as tf
import tensorflow.python.keras.backend as K
from tensorflow.python.keras import Input
from tensorflow.python.keras.models import Sequential, Model, model_from_yaml, load_model
from tensorflow.python.keras.layers import Dense, Flatten, Conv2D, LeakyReLU, concatenate, Dropout
from tensorflow.python.keras.callbacks import Callback, EarlyStopping, LearningRateScheduler, TensorBoard

# Data generator
class DataGenerator(tf.keras.utils.Sequence):
  'Generates data for tf.keras'
  def __init__(self, batch_size = 64, shuffle = True, datasettype = 'train', permFreq = 0.15, output_type = 'win'):
    'Initialization'
    self.datasettype = datasettype
    if datasettype == 'train': 
        self.link = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/train.csv'
    elif datasettype == 'val':
        self.link = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/val.csv'
    elif datasettype == 'test':
        self.link = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/test.csv'
    else:
        print("Incorrect datatype")
    self.xy = np.loadtxt(self.link, delimiter = ",", dtype = np.float32, skiprows = 1)
    np.random.shuffle(self.xy) # Shuffle dataset

    self.y = self.xy[:, [0]]
    self.f = self.xy[:, 1:6]
    self.x = self.xy[:, 6:58]
    self.x = self.x.reshape((-1,4,13,1))
    self.g = self.xy[:, 58:184]
    self.o = self.xy[:, 184:236]
    self.o = self.o.reshape((-1,4,13,1))

    self.dim = (4,13)
    self.batch_size = batch_size
    self.shuffle = shuffle
    self.indexes = np.arange(self.xy.shape[0])
    self.on_epoch_end()
    self.permFreq = permFreq
    self.output_type = output_type

  def __len__(self):
    'Denotes the number of batches per epoch'
    return int(np.floor(self.xy.shape[0] / self.batch_size))

  def __getitem__(self, index_batch):
    'Generate one batch of data'
    # Generate indexes of the batch
    indexes_batch = self.indexes[index_batch*self.batch_size:(index_batch+1)*self.batch_size]    
    # Generate data
    [x, f, g, o], y = self.__data_generation(indexes_batch)

    return [x, f, g, o], y

  def on_epoch_end(self):
    'Updates indexes after each epoch'
    if self.shuffle == True:
      np.random.shuffle(self.indexes)

  def __data_generation(self, indexes):
    'Generates data containing batch_size samples' # X : (n_samples, *dim, n_channels)
    # Initialization
    x = np.empty((self.batch_size, 4,13,1))
    f = np.empty((self.batch_size, 5))
    g = np.empty((self.batch_size, 126))
    o = np.empty((self.batch_size, 4,13,1))
    y = np.empty((self.batch_size))

    # Generate data
    for i, index in enumerate(indexes):

      # Random permutation for x and o
      r = random.random()
      if r < (1-self.permFreq):
        x[i,] = self.x[index,]
        o[i,] = self.o[index,]
      else:
        x[i,] = np.array([np.random.permutation(self.x[index,0,])])
        o[i,] = np.array([np.random.permutation(self.o[index,0,])])

      # f and g
      f[i,] = self.f[index,]
      g[i,] = self.g[index,]        

      # output type
      if self.output_type == 'win':
        if self.y[index,] > 0:
          y[i ] = 1
        else:
          y[i ] = 0
      elif self.output_type == 'score':
        y[i ] = self.y[index,]
      else: 
        print('Invalid input type')

    return [x, f, g, o], y

# TensorBoard
log_dir = "logs/fit/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir = log_dir, histogram_freq = 1)

# Checkpoint callback
checkpoint_path = "gdrive/My Drive/Colab Notebooks/GR-TrainModel/Checkpoint/cp.ckpt"
checkpoint_dir = os.path.dirname(checkpoint_path)
cp_callback = tf.keras.callbacks.ModelCheckpoint(filepath=checkpoint_path,
                                                 save_weights_only=True,
                                                 verbose=1,
                                                 save_freq = 'epoch')

# Step Decay
def step_decay_schedule(initial_lr=1e-3, decay_factor=0.75, step_size=10):
  def schedule(epoch):
    return initial_lr * (decay_factor ** np.floor(epoch/step_size))
  return LearningRateScheduler(schedule)
schedule = step_decay_schedule(initial_lr = 0.001, decay_factor=0.1, step_size = 50)

# Data generator
train_generator = DataGenerator(datasettype = 'train', batch_size = 128, permFreq = 0, output_type = 'score')
valid_generator = DataGenerator(datasettype = 'val', batch_size = 128, permFreq = 0, output_type = 'score')

def DualInception():
  x = Input(shape = (4,13,1,), name = 'x')
  f = Input(shape = (5,), name = 'f')
  g = Input(shape = (126,), name = 'g')
  o = Input(shape = (4,13,1,), name = 'o')

  # x1 = Flatten(name = 'xflatten')(x)
  # o1 = Flatten(name = 'oflatten')(o)
  
  # i = concatenate([x1, f, g, o1], name = 'concat')

  y = Dense(1, name = 'y')(f)

  model = Model(inputs = [x, f, g, o], outputs = y)
  # model.compile(loss = 'binary_crossentropy', optimizer = 'adam', metrics=['accuracy'])
  model.compile(loss = 'mse', optimizer = 'adam')
  return model

# Commented out IPython magic to ensure Python compatibility.
!rm -rf ./logs/ 
# %load_ext tensorboard
# %tensorboard --logdir logs/fit

# Model define
NN = DualInception()
NN.summary()
history_NN = NN.fit(x = train_generator,
                   validation_data = valid_generator,
                   epochs = 10,
                   callbacks = [cp_callback, schedule, tensorboard_callback])

test_generator = DataGenerator(datasettype = 'test', batch_size = 128, permFreq = 0, output_type = 'score')

NN.evaluate(test_generator)

"""# **Other model**"""

import matplotlib.pyplot as plt

data2 = pd.read_csv('gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/small_Simple_Last_OHE_noDup_ISRU_geo.csv')

geo = data.iloc[:,58:184]
sumgeo = geo.sum(axis = 1)
sumgeo = np.array(sumgeo).reshape(-1,1)

deadwood = data.Deadwood0
deadwood = np.array(deadwood).reshape(-1,1)

X = np.concatenate((sumgeo,deadwood),axis=1)

model = svm.SVC(probability=True)
model.fit(X, Y)

testDeadwood = np.arange(0, 100)
testDeadwood = testDeadwood.reshape(-1,1)
testGeo = np.full((100, 1), 5, dtype=int)

testX = np.concatenate((testGeo, testDeadwood), axis=1)
testY = model.predict(testX)

plt.plot(testDeadwood, testY)

testDeadwood = np.full((40, 1), 15, dtype=int)
testGeo = np.arange(0, 40)
testGeo = testGeo.reshape(-1,1)

testX = np.concatenate((testGeo, testDeadwood), axis=1)
testY = model.predict(testX)

plt.plot(testGeo, testY)

from sklearn import svm
X = data.Deadwood0
Y = data.win

X = np.array(X).reshape(-1, 1)

model = svm.SVC(probability=True)
model.fit(X, Y)

Yhat = model.predict(X)

1 - sum(Yhat - Y)/16317

testX = np.arange(0, 100)
testX = testX.reshape(-1,1)
testY = model.predict(testX)

model.predict_proba(testX)

plt.plot(testX, testY)

plt.scatter(X, Y)

data["win"] = np.nan

data.loc[data["score0_mean"] > 0 , "win"] = 1
data.loc[data["score0_mean"] <= 0 , "win"] = 0

data

data.describe()

corr_matrix = data.corr()

a = corr_matrix['win']

print(a.sort_values().index)

for i in a.sort_values():
  print(i)

import matplotlib.pyplot as plt
plt.figure(figsize=(60,7))
plt.plot(a)

import seaborn as sn

sn.heatmap(a, annot=True)
plt.show()



data  = pd.read_csv('gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/1M_Simple_All_OHE_noDup_ISRU_geo_val.csv')

data["win"] = np.nan

data.loc[data["score0_mean"] > 0 , "win"] = 1
data.loc[data["score0_mean"] <= 0 , "win"] = 0

y = np.array(data.win)

y

# f = np.array(data[['GamestateNum', 'RunNum0', 'SetNum0', 'Deadwood0', 'Hitscore0']])
f = np.array(data[['Deadwood0']])

data

f

y

y = y.reshape((-1,1))

model = svm.SVC(probability=True)
model.fit(f, y)

def DualInception():
  f = Input(shape = (1,), name = 'f')

  h = Dense(8, activation = 'sigmoid', name = 'h')(f)

  y = Dense(1, activation = 'sigmoid', name = 'y')(h)

  model = Model(inputs = f, outputs = y)
  model.compile(loss = 'binary_crossentropy', optimizer = 'adam', metrics=['accuracy'])
  return model

# Model define
NN = DualInception()
NN.summary()
history_NN = NN.fit(x = f,
                   y = y,
                   epochs = 10)

test.columns

GamestateNumTest = np.full((100, 1), 5, dtype=int)
RunNum0Test = np.full((100, 1), 1, dtype=int)
SetNum0Test = np.full((100, 1), 1, dtype=int)
Deadwood0Test = np.arange(0, 100)
Deadwood0Test = Deadwood0Test.reshape(-1,1)
Hitscore0Test = np.full((100, 1), 10, dtype=int)

testX = np.concatenate((GamestateNumTest, RunNum0Test, SetNum0Test, Deadwood0Test, Hitscore0Test), axis=1)
testY = NN.predict(testX)

data = pd.read_csv('gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/2M_Simple_All_OHE_randomplayer_nodup_ISRU_geo.csv')

data.describe()

data2 = pd.read_csv('gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/small_Simple_Last_OHE_noDup_ISRU_geo.csv')

plt.figure(figsize=(15,7))
plt.scatter(data2.Deadwood0, data2.score0_mean)

plt.figure(figsize=(15,7))
plt.scatter(data.Deadwood0, data.score0_mean)

plt.figure(figsize=(15,7))
plt.scatter(data.Hitscore0, data.score0_mean)