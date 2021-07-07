import torch
from torch_geometric.data import InMemoryDataset
import pandas as pd
from torch_geometric.data import Data

class MyOwnDataset():
  def __init__(self, df, n):
  	self.df = df
  	self.n = n
  
  	
  def process(self):
  	# Read data into huge `Data` list.
    edge_index = torch.tensor(self.c_e_index(), dtype=torch.long)
    x = torch.tensor(self.c_e_attr(), dtype=torch.float)

    data_n = Data(x=x, edge_index=edge_index)
    return data_n
	
    #data, slices = self.collate(data_list)
    #return data_list
    #torch.save((data, slices), self.processed_paths[0])
       
  def c_e_index(self):
    deck = self.df.columns.to_list()
    #create y
    m = self.df.values.tolist()[self.n]
    num1 = []
    num2 = []
    e = []
    for i in range(len(m)-1):
      if i > 51:
        a = ''
        b = ''
        if len(deck[i]) == 4:
          a = deck[i][0:2]
          b = deck[i][2:4]
        elif len(deck[i]) == 5:
          a = deck[i][0:2]
          b = deck[i][2:5]
        else:
          a = deck[i][0:3]
          b = deck[i][3:6]
        num1.append(deck.index(a))
        num2.append(deck.index(b))
        num1.append(deck.index(b))
        num2.append(deck.index(a))
    e = [num1, num2]
    return e

  def c_e_attr(self):
    m = self.df.values.tolist()[self.n]
    e = []
    for i in range(len(self.c_e_index()[0])):
      num = []
      if m[self.c_e_index()[0][i]] == 1 and m[self.c_e_index()[1][i]] == 1:
        num.append(1)
      else:
        num.append(0)
      e.append(num)
    return e


class Data_Process:
  def __init__(self, path):
    self.path = path
  
  def getGeoRelationVar(self):
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

  def addGeoRelation(self):
    X = pd.read_csv(self.path)
    X.drop(columns=['GamestateNum',    'MeldNum0',    'RunNum0',    'SetNum0',    'Deadwood0', 'Hitscore0'], inplace=True)
    y = X.pop('Score0')
    a = ''
    b = ''
    for i in self.getGeoRelationVar():
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
    geoData = pd.concat([X, y], axis = 1)
    #geoData.to_csv(pathOut, index=False)
    
    return geoData



path = '/Users/sangtruong_2021/Desktop/' + 'short_2M_Simple_All.csv'
dp = Data_Process(path)
df = dp.addGeoRelation()
data_list = []
for i in range(len(df.index)):
	ds = MyOwnDataset(df, i)
	df_ds = ds.process()
	data_list.append(df_ds)
print(data_list)
