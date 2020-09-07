import datetime
import tensorflow as tf
from tensorflow.python.keras.models import Sequential
from tensorflow.python.keras.layers import Dense
from tensorflow.python.keras.callbacks import EarlyStopping
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split

def NeuralNet_TF_0L():
  model = Sequential()
  model.add(Dense(1, input_dim = 178))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model
  
# Callback set-up
es = EarlyStopping(monitor='val_loss', verbose = 1, patience = 5, min_delta = 1)
log_dir = "logs/fit/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir = log_dir, histogram_freq = 1)

# Load and split data to X (hand only, no statistics)and y
dir = '/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-TrainModel/'
data = pd.read_csv(dir + "CumData_noDup_Geo.csv")

y = data.pop('score0_mean')
X = data.to_numpy()

# Split X and y to train and test
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.15, random_state=42)

# Training model
name = 'NeuralNet_TF_0L'
NN   =  NeuralNet_TF_0L ()
history_NN = NN.fit(X_train,
                   y_train,
                   validation_data = (X_test, y_test),
                   epochs = 100,
                   batch_size = 8,
                   callbacks = [es, tensorboard_callback])

# Compare with other model
print("y: \n", NN.predict(X_test[0:1]))

# Serialize model and weight to YAML
NN_yaml = NN.to_yaml()
with open(dir + name + ".yaml", "w") as yaml_file:
  yaml_file.write(NN_yaml)
NN.save_weights(dir + name + ".h5")
