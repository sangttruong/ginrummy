import pandas as pd
import math
from google.colab import drive
drive.mount('/content/gdrive/')
path = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/'
#df1 = pd.read_csv(path+'2M_Simple_All_OHE_noDup.csv')
df2 = pd.read_csv(path+'2M_Simple_All_OHE_noDup_ISRU.csv')
df2
column = df.columns.tolist()
for j in range(1000000):  
  s = 10
  s_temp = 0
  temp = []
  for i in range(57, 109):
    if df.at[j, column[i]] != 0 and df.at[j, column[i]] != 1:
      temp.append(i)
      s_temp += df.at[j, column[i]]
    if df.at[j, column[i]] == 1:
      s -= df.at[j, column[i]]

  for i in temp:
    df.at[j, column[i]] = df.at[j, column[i]]*s/s_temp
check = []
for j in range(2000000, 2600500):
  z = 0
  for i in range(57, 109):
    z += df.iloc[j].values[i]
  s_max = 10+0.00001
  s_min = 10-0.00001
  print(z)
check = []
z = 0
for i in range(57, 109):
  z += df.iloc[2600000].values[i]
s_max = 10+0.00001
s_min = 10-0.00001
print(z)
