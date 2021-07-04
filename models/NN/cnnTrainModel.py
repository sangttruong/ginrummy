import datetime
import tensorflow as tf
from tensorflow.python.keras.models import Sequential
from tensorflow.python.keras.layers import Dense
from tensorflow.python.keras.layers import Flatten
from tensorflow.python.keras.layers import Conv2D
from tensorflow.python.keras.callbacks import EarlyStopping
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split

from tensorflow.python.keras import Input
from tensorflow.python.keras.models import Model
from tensorflow.python.keras.layers import LeakyReLU

# Step Decay
import numpy as np
from tensorflow.python.keras.callbacks import LearningRateScheduler

def step_decay_schedule(initial_lr=1e-3, decay_factor=0.75, step_size=10):
  '''
  Wrapper function to create a LearningRateScheduler with step decay schedule.
  '''
  def schedule(epoch):
    return initial_lr * (decay_factor ** np.floor(epoch/step_size))

  return LearningRateScheduler(schedule)


def NeuralNet_TF_0L():
  model = Sequential()
  model.add(Dense(1, input_dim = 52))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_1L():
  model = Sequential()
  model.add(Dense(300, input_dim = 227, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00002) ))
  model.add(Dense(1))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_2L():
  model = Sequential()
  model.add(Dense(300, input_dim = 227, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001) ))
  model.add(Dense(300, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001)))
  model.add(Dense(1))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_3L():
  model = Sequential()
  model.add(Dense(300, input_dim = 227, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001) ))
  model.add(Dense(300, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001)))
  model.add(Dense(300, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001)))
  model.add(Dense(1))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_Functional_0L():
  inputs = Input(shape=(4,13,1,))
  # EXP 3
  # x = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu')(inputs)
  # x = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu')(x)

  # EXP 2
  # x = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'sigmoid')(inputs)
  # x = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'sigmoid')(x)

  # EXP 1
  x = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same')(inputs)
  x = LeakyReLU(alpha=0.3)(x)
  x = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same')(x)
  x = LeakyReLU(alpha=0.3)(x)
  x = Flatten()(x)
  outputs = Dense(1)(x)
  model = Model(inputs = inputs, outputs = outputs)
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

# Callback set-up
es = EarlyStopping(monitor='val_loss', verbose = 1, patience = 5, min_delta = 1)
log_dir = "logs/fit/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir = log_dir, histogram_freq = 1)
# schedule = SGDRScheduler(min_lr=1e-5,
#           max_lr=1e-2,
#           steps_per_epoch=np.ceil(epoch_size/batch_size),
#           lr_decay=0.9,
#           cycle_length=5,
#           mult_factor=1.5)
# schedule = step_decay_schedule(initial_lr=1e-4, decay_factor=0.75, step_size=2)


# Load and split data to X (hand only, no statistics)and y
# dir = '/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-TrainModel/'
dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/'
data = pd.read_csv(dir + "2M_Simple_All_noDup.csv")
y = data.pop('score0_mean')
features = pd.concat([data.pop(c) for c in ['GamestateNum','RunNum0','SetNum0','Deadwood0','Hitscore0']], axis = 1)
X = data.to_numpy()
X = X.reshape((-1,4,13,1)) # If use convolution operator

# Split X and y to train and test
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.15, random_state=42)

# EXP 1
# Training model
# name = 'NeuralNet_TF_0L_Geo_1M_onlyhand'
NN   = NeuralNet_TF_Functional_0L()
history_NN = NN.fit(X_train,
                   y_train,
                   validation_data = (X_test, y_test),
                   epochs = 100,
                   batch_size = 64,
                   callbacks = [es])

# EXP 2
# Training model
# name = 'NeuralNet_TF_0L_Geo_1M_onlyhand'
# NN   = NeuralNet_TF_Functional_0L()
# history_NN = NN.fit(X_train,
#                    y_train,
#                    validation_data = (X_test, y_test),
#                    epochs = 100,
#                    batch_size = 64,
#                    callbacks = [schedule, es])

# EXP 3


# Compare with other model
# print("y: \n", NN.predict(X_test[0:1]))

# Serialize model and weight to YAML
# NN_yaml = NN.to_yaml()
# with open(dir + name + ".yaml", "w") as yaml_file:
#  yaml_file.write(NN_yaml)
# NN.save_weights(dir + name + ".h5")




# ---------------------------------------------------------------------------------------------------
# Keras Callback for implementing Stochastic Gradient Descent with Restarts
from tensorflow.python.keras.callbacks import Callback
import tensorflow.python.keras.backend as K
import numpy as np

class SGDRScheduler(Callback):
  '''Cosine annealing learning rate scheduler with periodic restarts.

  # Usage
  ```python
    schedule = SGDRScheduler(min_lr=1e-5,
           max_lr=1e-2,
           steps_per_epoch=np.ceil(epoch_size/batch_size),
           lr_decay=0.9,
           cycle_length=5,
           mult_factor=1.5)
    model.fit(X_train, Y_train, epochs=100, callbacks=[schedule])
  ```

  # Arguments
  min_lr: The lower bound of the learning rate range for the experiment.
  max_lr: The upper bound of the learning rate range for the experiment.
  steps_per_epoch: Number of mini-batches in the dataset. Calculated as `np.ceil(epoch_size/batch_size)`. 
  lr_decay: Reduce the max_lr after the completion of each cycle.
      Ex. To reduce the max_lr by 20% after each cycle, set this value to 0.8.
  cycle_length: Initial number of epochs in a cycle.
  mult_factor: Scale epochs_to_restart after each full cycle completion.

  # References
  Blog post: jeremyjordan.me/nn-learning-rate
  Original paper: http://arxiv.org/abs/1608.03983
  '''
  def __init__(self, min_lr, max_lr, steps_per_epoch, lr_decay=1, cycle_length=10, mult_factor=2):

    self.min_lr = min_lr
    self.max_lr = max_lr
    self.lr_decay = lr_decay

    self.batch_since_restart = 0
    self.next_restart = cycle_length

    self.steps_per_epoch = steps_per_epoch

    self.cycle_length = cycle_length
    self.mult_factor = mult_factor

    self.history = {}

  def clr(self):
    '''Calculate the learning rate.'''
    fraction_to_restart = self.batch_since_restart / (self.steps_per_epoch * self.cycle_length)
    lr = self.min_lr + 0.5 * (self.max_lr - self.min_lr) * (1 + np.cos(fraction_to_restart * np.pi))
    return lr

  def on_train_begin(self, logs={}):
    '''Initialize the learning rate to the minimum value at the start of training.'''
    logs = logs or {}
    K.set_value(self.model.optimizer.lr, self.max_lr)

  def on_batch_end(self, batch, logs={}):
    '''Record previous batch statistics and update the learning rate.'''
    logs = logs or {}
    self.history.setdefault('lr', []).append(K.get_value(self.model.optimizer.lr))
    for k, v in logs.items():
      self.history.setdefault(k, []).append(v)

    self.batch_since_restart += 1
    K.set_value(self.model.optimizer.lr, self.clr())

  def on_epoch_end(self, epoch, logs={}):
    '''Check for end of current cycle, apply restarts when necessary.'''
    if epoch + 1 == self.next_restart:
      self.batch_since_restart = 0
      self.cycle_length = np.ceil(self.cycle_length * self.mult_factor)
      self.next_restart += self.cycle_length
      self.max_lr *= self.lr_decay
      self.best_weights = self.model.get_weights()

  def on_train_end(self, logs={}):
    '''Set weights to the values from the end of the most recent cycle for best performance.'''
    self.model.set_weights(self.best_weights)



# Step Decay
import numpy as np
from keras.callbacks import LearningRateScheduler

def step_decay_schedule(initial_lr=1e-3, decay_factor=0.75, step_size=10):
  '''
  Wrapper function to create a LearningRateScheduler with step decay schedule.
  '''
  def schedule(epoch):
    return initial_lr * (decay_factor ** np.floor(epoch/step_size))
  
  return LearningRateScheduler(schedule)
