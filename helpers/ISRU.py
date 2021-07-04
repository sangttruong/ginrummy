# **ISRU Function**
import math
def ISRU(x):
  a = 1
  return x / (math.sqrt(a + x**2))

y = []
x = []
import numpy as np
for i in np.arange(0,8,0.5):
  x.append(i)
  y.append(ISRU(i))

import matplotlib.pyplot as plt
plt.figure(figsize = (10,5))
plt.plot(x, y)
plt.show()

ISRU(0.23)

# **Convert Relative Likelihood to Probability using ISRU**
import pandas as pd
import math
from google.colab import drive
drive.mount('/content/gdrive/')

path = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/'
df = pd.read_csv(path+'2M_Simple_All_OHE_noDup.csv')

# Define Inverse Square Root Unit function
def ISRU(x):
  a = 1
  if x == 1 or x == 0:
    return x
  else:
    return x / (math.sqrt(a + x**2))
t = df.columns.tolist()

# Iterate through each e-column
for i in range(57, 109):
  df[t[i]] = df[t[i]].apply(ISRU)

# Save to csv
df.to_csv(path+'2M_Simple_All_OHE_noDup_ISRU.csv', index=False)
