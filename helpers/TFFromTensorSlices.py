import tensorflow as tf
import pathlib
import os
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split

np.set_printoptions(precision=4)

from google.colab import drive
drive.mount('/content/gdrive/')

xfoy = np.loadtxt('gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/2M_Simple_All_OHE_noDup_ISRU.csv', delimiter = ",", dtype = np.float32, skiprows = 1)
xfoy_train, xfoy_test = train_test_split(xfoy, test_size=0.15, random_state=42)

x_train = xfoy_train[:, 0:52] # exclude 52...
x_train = x_train.reshape((-1,1,4,13))
f_train = xfoy_train[:, 52:57] #exclude 57 ...
o_train = xfoy_train[:, 57:109] #excluding 109 ...
y_train = xfoy_train[:, [109]] #to keep the format as numpy array
n_samples_train = xfoy_train.shape[0]

x_test = xfoy_test[:, 0:52] # exclude 52...
x_test = x_test.reshape((-1,1,4,13))
f_test = xfoy_test[:, 52:57] #exclude 57 ...
o_test = xfoy_test[:, 57:109] #excluding 109 ...
y_test = xfoy_test[:, [109]] #to keep the format as numpy array
n_samples_test = xfoy_test.shape[0]

x_train_ds = tf.data.Dataset.from_tensor_slices(x_train)
f_train_ds = tf.data.Dataset.from_tensor_slices(f_train)
o_train_ds = tf.data.Dataset.from_tensor_slices(o_train)
y_train_ds = tf.data.Dataset.from_tensor_slices(y_train)

x_test_ds = tf.data.Dataset.from_tensor_slices(x_test)
f_test_ds = tf.data.Dataset.from_tensor_slices(f_test)
o_test_ds = tf.data.Dataset.from_tensor_slices(o_test)
y_test_ds = tf.data.Dataset.from_tensor_slices(y_test)
BATCH_SIZE = 64
SHUFFLE_BUFFER_SIZE = 100

# train_dataset = train_dataset.shuffle(SHUFFLE_BUFFER_SIZE).batch(BATCH_SIZE)

x_train_ds = x_train_ds.batch(BATCH_SIZE)
f_train_ds = f_train_ds.batch(BATCH_SIZE)
o_train_ds = o_train_ds.batch(BATCH_SIZE)
y_train_ds = y_train_ds.batch(BATCH_SIZE)

x_test_ds = x_test_ds.batch(BATCH_SIZE)
f_test_ds = f_test_ds.batch(BATCH_SIZE)
o_test_ds = o_test_ds.batch(BATCH_SIZE)
y_test_ds = y_test_ds.batch(BATCH_SIZE)
