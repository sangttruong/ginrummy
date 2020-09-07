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

# Generate the probability of win of last hand
#--------------------------------------------------------------------
# # Import features
# X = np.genfromtxt(path + 'features.txt', delimiter=' ')

# # Generate label from data of only simple player 0
# label = open(path + "gamestates.txt", "r")
# label = list(label)

# i = 1
# X_index = -1
# X_last = np.array([])
# y_last = []

# while i < len(label):
#     gs_info = list(map(int, label[i].split()))
#     if gs_info[3] == 0:
#         y_last.append(1)
#         X_index = X_index + gs_info[0]
#         X_last = np.append(X_last, X[X_index])

#     else:
#         y_last.append(0)
#         X_index = X_index + gs_info[0]
#         X_last = np.append(X_last, X[X_index])
#     i += gs_info[0] + 1

# X_last = X_last.reshape((-1, 8))
# y_last = np.array(y_last)

# # print(X_last)
# # print(y_last)

# Generate the score of last hand
#--------------------------------------------------------------------
# # Import features
# X = np.genfromtxt(path + 'features.txt', delimiter=' ')

# # Generate label from data of only simple player 0
# label = open(path + "gamestates.txt", "r")
# label = list(label)

# i = 1
# X_index = -1
# X_last = np.array([])
# y_last = []

# while i < len(label):
#     gs_info = list(map(int, label[i].split()))
#     if gs_info[3] == 0: 
#         y_last.append(gs_info[4])
#         X_index = X_index + gs_info[0]
#         X_last = np.append(X_last, X[X_index])

#     else:
#         y_last.append(0)
#         X_index = X_index + gs_info[0]
#         X_last = np.append(X_last, X[X_index])
#     i += gs_info[0] + 1

# X_last = X_last.reshape((-1, 8))
# y_last = np.array(y_last)

# Generate the probability of win of all hands
#--------------------------------------------------------------------
# # Import features
# X_all = np.genfromtxt(path + 'features.txt', delimiter=' ')

# # Generate label from data of only simple player 0
# label = open(path + "gamestates.txt", "r")
# label = list(label)

# i = 1
# y_all = []

# # Generate the probability of win
# while i < len(label):
#     gs_info = list(map(int, label[i].split()))
#     if gs_info[3] == 0:
#         local_y = np.ones(gs_info[0]) # If winner is 0: Append win
#         for j in local_y: y_all.append(j)
#     else:
#         local_y = np.zeros(gs_info[0]) # If winner is 1: Append lose 
#         for j in local_y: y_all.append(j)
#     i += gs_info[0] + 1
# y_all = np.array(y_all)

# Generate the score of all hands
#--------------------------------------------------------------------
# Import features
X_all = np.genfromtxt(path + 'features.txt', delimiter=' ')

# Generate label from data of only simple player 0
label = open(path + "gamestates.txt", "r")
label = list(label)

i = 1
y_all = []

while i < len(label):
    gs_info = list(map(int, label[i].split()))
    if gs_info[3] == 0:
        local_y = [gs_info[4]]*gs_info[0] # If winner is 0: Append win
        for j in local_y: y_all.append(j)
    else:
        local_y = np.zeros(gs_info[0]) # If winner is 1: Append lose 
        for j in local_y: y_all.append(j)
    i += gs_info[0] + 1
y_all = np.array(y_all)

#--------------------------------------------------------------------

# All or Last? 
X = X_all
y = y_all

#--------------------------------------------------------------------
# Normalization
y = (y - np.min(y))/(np.max(y)-np.min(y))

#--------------------------------------------------------------------
# Histogram
# plt.hist(y, bins = 30)
# plt.show()

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
plt.scatter(x, y_train, label = "y_test")
plt.scatter(x, pred_train, label = "predicted y",)
# plt.plot(y_test[0:100], label = "y_test")
# plt.plot(pred_test[0:100], label = "predicted y",)
plt.legend()
plt.show()

# Deploying model
#--------------------------------------------------------------------
# dump(xg_class, path + 'xgb.joblib')