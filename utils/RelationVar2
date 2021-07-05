from google.colab import drive
drive.mount('/content/gdrive/')
path = 'gdrive/My Drive/Colab Notebooks/Gin2/'
import pandas as pd
import statsmodels.api as sm
import matplotlib.pyplot as plt
# **Prepare the column**
m = ['A', 'B', 'C', 'D']
f = []
for i in range(1, 14):
  f.append(m)
# print(f)

result = []
s1 = 0
for i in range(len(f)):
  for j in range(4):
    t = j
    temp = []
    while(t < 3):
      s = str(i+1) + f[i][j] + str(i+1) + f[i][t+1]
      temp.append(s)
      t += 1
    if i < len(f)-1:
      s = str(i+1) + f[i][j] + str(i+2) + f[i+1][j]
      temp.append(s)
    if i < len(f) - 2:
      s = str(i+1) + f[i][j] + str(i+3) + f[i+1][j]
      temp.append(s)
    s1 += len(temp)
    result.append(temp)
# print(result.remove([]))
# print(result)
# print(s1)

# List of new columns
c_new = []
for i in range(len(result)):
  for j in range(len(result[i])):
    c_new.append(result[i][j])

print(c_new)
print(len(c_new))
# Double check 
print(len(c_new))
from collections import OrderedDict
c_new2 = list(OrderedDict.fromkeys(c_new))
print(len(c_new2))
# **Import the .csv file to excute**
data = pd.read_csv(path+'TestingDummyHand0_1.csv')
data
y = data['Score0']
X = data.drop(columns= 'Score0')
print(X)
print(y)
# **Instantiate new columns**
X_geo = pd.DataFrame()
a = ''
b = ''
for i in c_new:
  if len(i) == 4:
    a = i[0:2]
    b = i[2:4]
  elif len(i) == 5:
    a = i[0:2]
    b = i[2:5]
  else:
    a = i[0:3]
    b = i[3:6]
  X_geo[i] = X[a] * X[b]
X_geo
XX_geo = pd.concat([X, X_geo], axis = 1)
XX_geo.to_csv(path + 'Processed_TestingDummyHand0_1.csv', index=False)

# **Regression**
XX_geo
XX_geo_const = sm.add_constant(XX_geo)
lm = sm.OLS(y, XX_geo_const).fit()
lm.summary()
parameters = lm.params
p = parameters.to_frame()
p.to_csv(path + 'LMGeo_parameters_2.csv')
