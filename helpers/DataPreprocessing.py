import pandas as pd
from sklearn.model_selection import train_test_split
import math

# Return a list of geo-relation variables
def getGeoRelationVar():
  suit = ['A', 'B', 'C', 'D']
  f = []
  for i in range(1, 14):
    f.append(suit)

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
      s1 += len(temp)
      result.append(temp)
  # List of new columns
  c_new = []
  for i in range(len(result)):
    for j in range(len(result[i])):
      c_new.append(result[i][j])

  return c_new

# Return a list of expanded geo-relation variables
def getExpGeoRelationVar():
  suit = ['A', 'B', 'C', 'D']
  f = []
  for i in range(1, 14):
    f.append(suit)

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
  # List of new columns
  c_new = []
  for i in range(len(result)):
    for j in range(len(result[i])):
      c_new.append(result[i][j])

  return c_new

# Return a list of 52 cards
def getDeck ():
  deck = ['1A', '2A', '3A', '4A', '5A', '6A', '7A', '8A', '9A', '10A', '11A', '12A', '13A',
          '1B', '2B', '3B', '4B', '5B', '6B', '7B', '8B', '9B', '10B', '11B', '12B', '13B',
          '1C', '2C', '3C', '4C', '5C', '6C', '7C', '8C', '9C', '10C', '11C', '12C', '13C',
          '1D', '2D', '3D', '4D', '5D', '6D', '7D', '8D', '9D', '10D', '11D', '12D', '13D']
  
  return deck

def getEDeck ():
  eDeck = ['e1A','e2A','e3A','e4A','e5A','e6A','e7A','e8A','e9A','e10A','e11A','e12A','e13A',
	   'e1B','e2B','e3B','e4B','e5B','e6B','e7B','e8B','e9B','e10B','e11B','e12B','e13B',
	   'e1C','e2C','e3C','e4C','e5C','e6C','e7C','e8C','e9C','e10C','e11C','e12C','e13C',
	   'e1D','e2D','e3D','e4D','e5D','e6D','e7D','e8D','e9D','e10D','e11D','e12D','e13D']

  return eDeck

# Take hand dataframe (52 cards) and scores and remove the duplicated hands
def rmDuplicatedHand (pathIn, pathOut):
  df = pd.read_csv(pathIn)
  df = df.groupby(getDeck()+ ['GamestateNum','RunNum0','SetNum0','Deadwood0','Hitscore0'] + getEDeck()).agg({'Score0': ['mean']})
  # df = df.groupby(getDeck()).agg({'Score0': ['mean']})
  df.columns = ['score0_mean']
  df = df.reset_index()
  df.sort_values(by=getDeck(), inplace = True)
  df.to_csv(pathOut, index=False)
  return df

# Take a hand dataframe (52 cards) and scores, then add geo-relation variables
def addGeoRelation(pathIn, pathOut):
  X = pd.read_csv(pathIn)
  nonX = pd.concat([X.pop(c) for c in ['GamestateNum','score0_mean','RunNum0','SetNum0','Deadwood0','Hitscore0']], axis = 1)
  prob = pd.concat([X.pop(c) for c in getEDeck()], axis = 1)
  # nonX = pd.concat([X.pop(c) for c in ['score0_mean', 'Hitscore0']], axis = 1)
  # nonX = pd.concat([X.pop(c) for c in ['score0_mean']], axis = 1)
  a = ''
  b = ''
  for i in getGeoRelationVar():
    if len(i) == 4:
      a = i[0:2]
      b = i[2:4]
    elif len(i) == 5:
      a = i[0:2]
      b = i[2:5]
    else:
      a = i[0:3]
      b = i[3:6]
    X[i] = X[a] * X[b]
  geoData = pd.concat([X, nonX, prob], axis = 1)  
  geoData.to_csv(pathOut, index=False)
  
  return geoData

# Take a hand dataframe (52 cards) and scores, then add expanded geo-relation variables
def addExpGeoRelation(pathIn, pathOut):
  X = pd.read_csv(pathIn)
  nonX = pd.concat([X.pop(c) for c in ['GamestateNum','score0_mean','RunNum0','SetNum0','Deadwood0','Hitscore0']], axis = 1)
  prob = pd.concat([X.pop(c) for c in getEDeck()], axis = 1)
  # nonX = pd.concat([X.pop(c) for c in ['score0_mean', 'Hitscore0']], axis = 1)
  # nonX = pd.concat([X.pop(c) for c in ['score0_mean']], axis = 1)
  a = ''
  b = ''
  for i in getExpGeoRelationVar():
    if len(i) == 4:
      a = i[0:2]
      b = i[2:4]
    elif len(i) == 5:
      a = i[0:2]
      b = i[2:5]
    else:
      a = i[0:3]
      b = i[3:6]
    X[i] = X[a] * X[b]
  geoData = pd.concat([X, nonX, prob], axis = 1)  
  geoData.to_csv(pathOut, index=False)

  return geoData

# Define Inverse Square Root Unit function
def ISRU(x):
  a = 1
  if x == 1 or x == 0:
    return x
  else:
    return x / (math.sqrt(a + x*x))

# Convert Relative Likelihood to Probability using ISRU
def normalization(pathIn, pathOut):
  df = pd.read_csv(pathIn)
  t = df.columns.tolist()
  for i in range(57, 109):
    df[t[i]] = df[t[i]].apply(ISRU)
  df.to_csv(pathOut, index=False)

  return df

# GamestateNum,Score0,MeldNum0,RunNum0,SetNum0,Deadwood0,Hitscore0,
# 1A,2A,3A,4A,5A,6A,7A,8A,9A,10A,11A,12A,13A,1B,2B,3B,4B,5B,6B,7B,8B,9B,10B,11B,12B,13B,
# 1C,2C,3C,4C,5C,6C,7C,8C,9C,10C,11C,12C,13C,1D,2D,3D,4D,5D,6D,7D,8D,9D,10D,11D,12D,13D,
# e1A,e2A,e3A,e4A,e5A,e6A,e7A,e8A,e9A,e10A,e11A,e12A,e13A,e1B,e2B,e3B,e4B,e5B,e6B,e7B,e8B,e9B,e10B,e11B,e12B,e13B,
# e1C,e2C,e3C,e4C,e5C,e6C,e7C,e8C,e9C,e10C,e11C,e12C,e13C,e1D,e2D,e3D,e4D,e5D,e6D,e7D,e8D,e9D,e10D,e11D,e12D,e13D

# Score0, GamestateNum,MeldNum0,RunNum0,SetNum0,Deadwood0,Hitscore0,
# 1A,2A,3A,4A,5A,6A,7A,8A,9A,10A,11A,12A,13A,1B,2B,3B,4B,5B,6B,7B,8B,9B,10B,11B,12B,13B,
# 1C,2C,3C,4C,5C,6C,7C,8C,9C,10C,11C,12C,13C,1D,2D,3D,4D,5D,6D,7D,8D,9D,10D,11D,12D,13D,
# e1A,e2A,e3A,e4A,e5A,e6A,e7A,e8A,e9A,e10A,e11A,e12A,e13A,e1B,e2B,e3B,e4B,e5B,e6B,e7B,e8B,e9B,e10B,e11B,e12B,e13B,
# e1C,e2C,e3C,e4C,e5C,e6C,e7C,e8C,e9C,e10C,e11C,e12C,e13C,e1D,e2D,e3D,e4D,e5D,e6D,e7D,e8D,e9D,e10D,e11D,e12D,e13D

# Remove duplicated
rmDuplicatedHand('/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_OHE_deploy.csv',
		 '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_OHE_noDup_deploy.csv')

# score0_mean, GamestateNum,MeldNum0,RunNum0,SetNum0,Deadwood0,Hitscore0,
# 1A,2A,3A,4A,5A,6A,7A,8A,9A,10A,11A,12A,13A,1B,2B,3B,4B,5B,6B,7B,8B,9B,10B,11B,12B,13B,
# 1C,2C,3C,4C,5C,6C,7C,8C,9C,10C,11C,12C,13C,1D,2D,3D,4D,5D,6D,7D,8D,9D,10D,11D,12D,13D,
# e1A,e2A,e3A,e4A,e5A,e6A,e7A,e8A,e9A,e10A,e11A,e12A,e13A,e1B,e2B,e3B,e4B,e5B,e6B,e7B,e8B,e9B,e10B,e11B,e12B,e13B,
# e1C,e2C,e3C,e4C,e5C,e6C,e7C,e8C,e9C,e10C,e11C,e12C,e13C,e1D,e2D,e3D,e4D,e5D,e6D,e7D,e8D,e9D,e10D,e11D,e12D,e13D

normalization('/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_OHE_noDup_deploy.csv',
	      '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_OHE_noDup_ISRU_deploy.csv')

# score0_mean, GamestateNum,MeldNum0,RunNum0,SetNum0,Deadwood0,Hitscore0,
# 1A,2A,3A,4A,5A,6A,7A,8A,9A,10A,11A,12A,13A,1B,2B,3B,4B,5B,6B,7B,8B,9B,10B,11B,12B,13B,
# 1C,2C,3C,4C,5C,6C,7C,8C,9C,10C,11C,12C,13C,1D,2D,3D,4D,5D,6D,7D,8D,9D,10D,11D,12D,13D,
# e1A,e2A,e3A,e4A,e5A,e6A,e7A,e8A,e9A,e10A,e11A,e12A,e13A,e1B,e2B,e3B,e4B,e5B,e6B,e7B,e8B,e9B,e10B,e11B,e12B,e13B,
# e1C,e2C,e3C,e4C,e5C,e6C,e7C,e8C,e9C,e10C,e11C,e12C,e13C,e1D,e2D,e3D,e4D,e5D,e6D,e7D,e8D,e9D,e10D,e11D,e12D,e13D

# Add geo-relation
addGeoRelation('/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_noDup.csv',
	       '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_noDup_geo.csv')

# score0_mean, GamestateNum,MeldNum0,RunNum0,SetNum0,Deadwood0,Hitscore0,
# 1A,2A,3A,4A,5A,6A,7A,8A,9A,10A,11A,12A,13A,1B,2B,3B,4B,5B,6B,7B,8B,9B,10B,11B,12B,13B,
# 1C,2C,3C,4C,5C,6C,7C,8C,9C,10C,11C,12C,13C,1D,2D,3D,4D,5D,6D,7D,8D,9D,10D,11D,12D,13D,
# geo-relation
# e1A,e2A,e3A,e4A,e5A,e6A,e7A,e8A,e9A,e10A,e11A,e12A,e13A,e1B,e2B,e3B,e4B,e5B,e6B,e7B,e8B,e9B,e10B,e11B,e12B,e13B,
# e1C,e2C,e3C,e4C,e5C,e6C,e7C,e8C,e9C,e10C,e11C,e12C,e13C,e1D,e2D,e3D,e4D,e5D,e6D,e7D,e8D,e9D,e10D,e11D,e12D,e13D

# Add expanded geo-relation
# addExpGeoRelation('/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_OHE_noDup_deploy.csv',
#                  '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/2M_Simple_All_OHE_noDup_deploy_expGeo.csv')
