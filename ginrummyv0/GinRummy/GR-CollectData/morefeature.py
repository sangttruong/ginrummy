# Import packages
path = '/Users/sangtruong_2021/Documents/GitHub/GinRummy/GinRummy/data/'

import numpy as np
import sklearn.model_selection
import math
import xgboost as xgb
from sklearn.metrics import mean_squared_error
from sklearn.model_selection import train_test_split
import pickle
from joblib import dump
import matplotlib.pyplot as plt

# Generate the score of last hand
#--------------------------------------------------------------------
# Import features

i = 0
X = open(path + "features_new.txt", "r")
X = list(X)
# print(X[0])

num_states = []
features = []
while i < len(X):
    num_state = int(X.pop(i))
    num_states.append(num_state)
    i = i + num_state
# print(num_states)

import ast
for i in X: 
    i = ast.literal_eval(i)
    features.append(i)
features = np.array(features)
# print(features)

# Generate label from data of only simple player 0
label = open(path + "gamestates_new.txt", "r")
label = list(label)

i = 1
features_index = -1
features_last = np.array([])
y_last = []

j = 0

while i < len(label):
    gs_info = list(map(int, label[i].split()))
    if gs_info[3] == 0: 
        y_last.append(gs_info[4])
        features_index = features_index + num_states[j]
        features_last = np.append(features_last, features[features_index])

    else:
        y_last.append(0)
        features_index = features_index + num_states[j]
        features_last = np.append(features_last, features[features_index])
    j = j + 1
    i = i + gs_info[0] + 1

features_last = features_last.reshape((-1, 11))
print(features_last)
y_last = np.array(y_last)
print(y_last)

#--------------------------------------------------------------------
# All or Last? 
X = features_last
y = y_last

# Create DMatrix data structure for XGB
#--------------------------------------------------------------------
data_dmatrix = xgb.DMatrix(data = X, label = y)

# Data splitting
#--------------------------------------------------------------------
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.2, random_state = 42)

# XGBoost with features
#--------------------------------------------------------------------
xg_class = xgb.XGBRegressor(objective ='reg:squarederror', colsample_bytree = 0.3, learning_rate = 0.1, max_depth = 10, alpha = 10, n_estimators = 10)
xg_class.fit(X_train,y_train)
pred_train = xg_class.predict(X_train)
pred_test = xg_class.predict(X_test)

# Cross validation
#--------------------------------------------------------------------
params = {"objective":"reg:squarederror",'colsample_bytree': 0.3,'learning_rate': 0.1, 'max_depth': 5, 'alpha': 10}
cv_results = xgb.cv(dtrain = data_dmatrix, params = params, nfold = 20, num_boost_round = 50, early_stopping_rounds = 20, as_pandas = True, seed = 42)
print(cv_results)

# Visualization
#--------------------------------------------------------------------
x = np.arange(len(y_train))
# plt.scatter(x, y_train, label = "y_test")
# plt.scatter(x, pred_train, label = "predicted y",)
plt.plot(y_test, label = "y_test")
plt.plot(pred_test, label = "predicted y",)
plt.legend()
plt.show()

# Deploying model
#--------------------------------------------------------------------
# dump(xg_class, path + 'xgb.joblib')