import numpy as np
import sys
import os
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')
sys.stderr = stderr

# disable any warnings by TensorFlow.
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'


import datetime
import random
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split

import tensorflow as tf
#import tensorflow.python.keras.backend as K
from tensorflow.python.keras import Input
from tensorflow.python.keras.models import Sequential, Model, model_from_yaml, load_model
from tensorflow.python.keras.layers import Dense, Flatten, Conv2D, LeakyReLU, concatenate, Dropout
from tensorflow.python.keras.callbacks import Callback, EarlyStopping, LearningRateScheduler

def DualInception():
  x = Input(shape = (4,13,1,), name = 'x')
  f = Input(shape = (4,), name = 'f')
  g = Input(shape = (126,), name = 'g')
  o = Input(shape = (4,13,1,), name = 'o')

  x1 = Flatten(name = 'x1')(x)  
  o1 = Flatten(name = 'o1')(o)

  i = concatenate([f, o1, x1], name = 'concat')

  i1 = Dense(16, activation = 'relu', name = 'dense1')(i)

  y = Dense(1, name = 'y')(i1)

  model = Model(inputs = [x, f, g, o], outputs = y)
  model.compile(loss = 'mse', optimizer = 'adam')
  return model
      
NN = DualInception()
checkpoint_path = '/Users/sangtruong_2021/Desktop/Checkpoint/cp.ckpt'
NN.load_weights(checkpoint_path)
print(NN.summary())

def addGeoRelation_Numpy(x):
    t = []
    for i in range(13):
        n = i
        while(n < 52):
            z = 1
            next = n+13*z
            while(next < 52):
                t.append(x[n]*x[next])
                z += 1
                next = n+13*z
            if n != 12 and n != 25 and n != 38 and n != 51:
                t.append(x[n]*x[n+1])
            n += 13
    return np.array(t)

import socket
import ast

listensocket = socket.socket()
listensocket.bind(("127.0.0.1", 8888))
listensocket.listen(999999999)
print("Server started at 127.0.0.1 on port " + str(8888))

running = True
while running:
  (clientsocket, address) = listensocket.accept()
  # print("New connection make!")
  xfo = clientsocket.recv(1024).decode() # Get a number from Java
  xfo = xfo[1:-2]
  xfo = list(map(float, xfo.split(", ")))
  xfo = np.array(xfo)

  x = xfo[0:52]
  g = addGeoRelation_Numpy(x)
  g = g.reshape((-1,126))
  x = x.reshape((-1,4,13,1))
  f = xfo[53:57]
  f = f.reshape((-1,4))
  o = xfo[57:109]
  o = o.reshape((-1,4,13,1))
  
#  print(x)
#  print(g)
#  print(f)
#  print(o)
  
  y = NN([x, f, g, o])
  # print(y)
  y = y.numpy()
  y = y[0][0]
#  print(y)
#  print(type(y))
  
  newMessage = str(y)
  
#  print("Output: " + newMessage)
  clientsocket.send(newMessage.encode()) # Send a number back to Java
  #print("Computed and sent!")
  clientsocket.close()
