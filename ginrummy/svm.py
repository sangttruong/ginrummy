# Import packages
path = '/Users/sangtruong_2021/Documents/GitHub/GinRummy/GinRummyEAAI_Hoang/'
from joblib import dump, load
import pandas as pd

# Import model
svm = load(path + 'svm.joblib') 

# Evaluation function
pred = svm.predict_proba([[1,1,1,1,1,1,1,1]])

# Output prediction for Java
f = open(path + "pre.txt", "w")
f.write(str(pred[0][1]))
f.close()