# Import packages
path = '/Users/sangtruong_2021/Documents/GitHub/GinRummy/GinRummyEAAI_Hoang/'
from joblib import load
import pandas as pd
import numpy as np

# Import model
xgb = load(path + 'xgb.joblib') 

# XGB only take input as numpy array
xgbinput = np.array([1,1,1,1,1,1,1,1]).reshape((1, 8))

# Evaluation function
pred = xgb.predict_proba(xgbinput)
print(pred) # Probability of lose (0) and win (1) in that order. 

# Output prediction for Java
f = open(path + "pre.txt", "w")
f.write(str(pred[0][1]))
f.close()