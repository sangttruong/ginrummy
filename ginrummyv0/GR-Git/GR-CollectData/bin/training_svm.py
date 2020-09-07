# Import packages
path = '/Users/sangtruong_2021/Documents/GitHub/GinRummy/GinRummyEAAI_Hoang/'

import numpy as np
import pandas as pd
import sklearn.model_selection
import math
from sklearn.svm import SVC
from sklearn.model_selection import train_test_split
import pickle
from joblib import dump, load
from numpy import genfromtxt

# Import features
X = genfromtxt(path + 'features.txt', delimiter=' ')

# Generate label from data of only simple player 0
label = open(path + "gamestates.txt", "r")
label = list(label)

i = 1
y = []
while i < len(label):
    gs_info = list(map(int, label[i].split()))
    if gs_info[3] == 0:
        local_y = np.ones(gs_info[0]) # If winner is 0: Append win
        for j in local_y: y.append(j)
    else:
        local_y = np.zeros(gs_info[0]) # If winner is 1: Append lose 
        for j in local_y: y.append(j)
    i += gs_info[0] + 1
y = np.array(y)
# y.reshape(39, -1)

# Data splitting
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.1, random_state = 42)

# SVM with features
svm = SVC(kernel = 'rbf', probability = True)
svm.fit(X_train, y_train)

# Mean accuracy
print("Training score: ------------------------")
print(svm.score(X_train, y_train))
print("Testing score: -------------------------")
print(svm.score(X_test, y_test))

# Deploying model
dump(svm, path + 'svm.joblib')