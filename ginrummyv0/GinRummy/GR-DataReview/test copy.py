import pandas as pd
import numpy as np

path = '/Users/sangtruong_2021/Desktop/'
data = pd.read_csv(path + 'big.csv')

print(data.head())

from sklearn.model_selection import train_test_split

# print(data.columns)
y = np.array(data['Score'])
X = np.array(data[['Deadwood', 'Deadwood_1_10', 'Deadwood_0', 'Hitscore', 'MeldNum', 'HitscoreMeldNum', 'HitscoreDeadwood' ]])

# from sklearn.preprocessing import PolynomialFeatures
# poly = PolynomialFeatures(4)
# polyX = poly.fit_transform(X)

# Data splitting
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1, random_state=42)

from sklearn.linear_model import LinearRegression
from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense
from keras.models import model_from_yaml
from keras.layers import LeakyReLU

model = Sequential()
model.add(Dense(1, input_dim=7))
# model.add(LeakyReLU(alpha=0.1))

# model.add(Dense(1))
# model.add(LeakyReLU(alpha=0.1))

# compile the keras model
# https://www.tensorflow.org/api_docs/python/tf/keras/metrics/RootMeanSquaredError
model.compile(loss= 'mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])

# fit the keras model on the dataset
model.fit(X, y, epochs=10, batch_size=32)

print(model.layers[0].get_weights())

# # Evaluate the keras model
# _, MSE = model.evaluate(X, y)
# print('Mean_squared_error: %.2f' % MSE)

# # serialize model to YAML
# model_yaml = model.to_yaml()
# with open("model.yaml", "w") as yaml_file: yaml_file.write(model_yaml)

# # serialize weights to HDF5
# model.save_weights("model.h5")
# print("Saved model to disk")
 
# # load YAML and create model
# yaml_file = open('model.yaml', 'r')
# loaded_model_yaml = yaml_file.read()
# yaml_file.close()
# loaded_model = model_from_yaml(loaded_model_yaml)

# # load weights into new model
# loaded_model.load_weights("model.h5")
# print("Loaded model from disk")
 
# # evaluate loaded model on test data
# loaded_model.compile(loss= 'mean_squared_error', optimizer='adam', metrics=['mean_squared_error'])
# score = loaded_model.evaluate(X, y, verbose=0)
# print("%s: %.2f%%" % (loaded_model.metrics_names[1], score[1]))