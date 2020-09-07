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

# Import features
X = np.genfromtxt(path + 'features.txt', delimiter=' ')

# Generate label from data of only simple player 0
label = open(path + "gamestates.txt", "r")
label = list(label)

i = 1
y = []

# Generate the probability of win
# while i < len(label):
#     gs_info = list(map(int, label[i].split()))
#     if gs_info[3] == 0:
#         local_y = np.ones(gs_info[0]) # If winner is 0: Append win
#         for j in local_y: y.append(j)
#     else:
#         local_y = np.zeros(gs_info[0]) # If winner is 1: Append lose 
#         for j in local_y: y.append(j)
#     i += gs_info[0] + 1
# y = np.array(y)

# Generate the score
while i < len(label):
    gs_info = list(map(int, label[i].split()))
    if gs_info[3] == 0:
        local_y = [gs_info[4]]*gs_info[0] # If winner is 0: Append win
        for j in local_y: y.append(j)
    else:
        local_y = np.zeros(gs_info[0]) # If winner is 1: Append lose 
        for j in local_y: y.append(j)
    i += gs_info[0] + 1
y = np.array(y)

# y.reshape(39, -1)

# Create DMatrix data structure for XGB
data_dmatrix = xgb.DMatrix(data = X, label = y)

# Data splitting
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.2, random_state = 42)

# XGBoost with features
xg_class = xgb.XGBRegressor(objective ='reg:squarederror', colsample_bytree = 0.3, learning_rate = 0.1, max_depth = 10, alpha = 10, n_estimators = 10)
xg_class.fit(X_train,y_train)
pred_test = xg_class.predict(X_test)

# Cross validation
params = {"objective":"reg:squarederror",'colsample_bytree': 0.3,'learning_rate': 0.1, 'max_depth': 5, 'alpha': 10}
cv_results = xgb.cv(dtrain = data_dmatrix, params = params, nfold = 20, num_boost_round = 50, early_stopping_rounds = 20, as_pandas = True, seed = 42)
print(cv_results)

# Visualization
plt.plot(y_test, label = "y_test")
plt.plot(pred_test, label = "predicted y",)
plt.legend()
plt.show()

print("Done")
# Deploying model
# dump(xg_class, path + 'xgb.joblib')