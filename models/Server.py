import numpy as np
import sys
import os
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')
sys.stderr = stderr
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2' # disable any warnings by TensorFlow.
import datetime
import random
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
import tensorflow as tf
from tensorflow.python.keras import Input
from tensorflow.python.keras.models import Sequential, Model, model_from_yaml, load_model
from tensorflow.python.keras.layers import Dense, Flatten, Conv2D, LeakyReLU, concatenate, Dropout
from tensorflow.python.keras.callbacks import Callback, EarlyStopping, LearningRateScheduler

def DualInception():
  x = Input(shape = (4,13,1,), name = 'x')
  f = Input(shape = (4,), name = 'f')
  g = Input(shape = (126,), name = 'g')
  o = Input(shape = (4,13,1,), name = 'o')

  x41 = Conv2D(filters = 4, kernel_size = 4, strides = 1, padding = 'valid', activation = 'relu', data_format = 'channels_last', name = 'x41')(x)
  x4 = Flatten(name = 'x4')(x41)

  x31 = Conv2D(filters = 4, kernel_size = 3, strides = 1, padding = 'valid', activation = 'relu', data_format = 'channels_last', name = 'x31')(x)
  x3 = Flatten(name = 'x3')(x31)

  x21 = Conv2D(filters = 4, kernel_size = 2, strides = 1, padding = 'valid', activation = 'relu', data_format = 'channels_last', name = 'x21')(x)
  x2 = Flatten(name = 'x2')(x21)  

  o41 = Conv2D(filters = 4, kernel_size = 4, strides = 1, padding = 'valid', activation = 'relu', data_format = 'channels_last', name = 'o41')(o)
  o4 = Flatten(name = 'o4')(o41)
  
  o31 = Conv2D(filters = 4, kernel_size = 3, strides = 1, padding = 'valid', activation = 'relu', data_format = 'channels_last', name = 'o31')(o)
  o3 = Flatten(name = 'o3')(o31)

  o21 = Conv2D(filters = 4, kernel_size = 2, strides = 1, padding = 'valid', activation = 'relu', data_format = 'channels_last', name = 'o21')(o)
  o2 = Flatten(name = 'o2')(o21)

  i = concatenate([f, o4, o3, o2, x4, x3, x2], name = 'concat')

  i1 = Dense(16, activation = 'relu', name = 'dense1')(i)

  y = Dense(1, name = 'y')(i1)

  model = Model(inputs = [x, f, g, o], outputs = y)
  model.compile(loss = 'mse', optimizer = 'adam')
  return model
      
NN = DualInception()
# checkpoint_path = 'DualInception/cp.ckpt'
checkpoint_path = 'D:\\Github\\ginrummy\\TruongNeller2020\\ginrummy\\DualInception\\cp.ckpt'
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
  xfo = xfo[1:-3]
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
  y = NN([x, f, g, o])
  y = y.numpy()
  y = y[0][0]  
  newMessage = str(y)
  clientsocket.send(newMessage.encode()) # Send a number back to Java
  #print("Computed and sent!")
  clientsocket.close()
