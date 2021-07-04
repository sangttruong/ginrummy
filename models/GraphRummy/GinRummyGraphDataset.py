import torch
from torch_geometric.data import InMemoryDataset
from torch_geometric.data import Data
from torch_geometric.data import Dataset
import pandas as pd

class GinRummyDataset(InMemoryDataset):
	df = ""
	data_list = []
	def __init__(self, path):
		self.path = path
		self.func()
	#Implement process method from InMemoryDataset superclass

	def preprocess(self):
		# Read data into huge `Data` list.
		for i in range(len(self.df.index)):
			edge_index = torch.tensor(self.c_e_index(i), dtype=torch.long)
			x = torch.tensor(self.c_vector_X(i), dtype=torch.float)
			edge_attr = torch.tensor(self.c_e_attr(i), dtype=torch.float)
			y = torch.tensor([self.get_y(i)], dtype=torch.float)
			data_n = Data(x=x, edge_index=edge_index, edge_attr=edge_attr, y=y)
			self.data_list.append(data_n)
		return self.data_list
		
	def process(self):
		data, slices = self.collate(self.data_list)
		torch.save((data, slices), 'graph_short_2M_Simple_All.pt')

	# Create edge index
	def c_e_index(self, n):
		deck = self.df.columns.to_list()
		#create y
		m = self.df.values.tolist()[n]
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
  
	# Create edge attribute
	def c_e_attr(self, n):
		m = self.df.values.tolist()[n]
		e = []
		for i in range(len(self.c_e_index(n)[0])):
			num = []
			if m[self.c_e_index(n)[0][i]] == 1 and m[self.c_e_index(n)[1][i]] == 1:
				num.append(1)
			else:
				num.append(0)
			e.append(num)
		return e
	
	# Create vector x
	def c_vector_X(self, n):
		deck = self.df.columns.to_list()
		m = self.df.values.tolist()[n]
		#create x
		x = []
		t = []
		for i in range(len(self.getDeck())):
			num = []
			num.append(int(deck[i][0:len(deck[i])-1]))
			num.append(m[i])
			x.append(num)
		return x
	
	# Get y value
	def get_y(self, n):
		m = self.df.values.tolist()[n]
		return m[len(m)-1]
	
	def getDeck(self):
		deck = ['1A', '2A', '3A', '4A', '5A', '6A', '7A', '8A', '9A', '10A', '11A', '12A', '13A',
				'1B', '2B', '3B', '4B', '5B', '6B', '7B', '8B', '9B', '10B', '11B', '12B', '13B',
				'1C', '2C', '3C', '4C', '5C', '6C', '7C', '8C', '9C', '10C', '11C', '12C', '13C',
				'1D', '2D', '3D', '4D', '5D', '6D', '7D', '8D', '9D', '10D', '11D', '12D', '13D']
		return deck

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
		X.drop(columns=['GamestateNum','MeldNum0','RunNum0','SetNum0','Deadwood0', 'Hitscore0'], inplace=True)
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
		self.df = pd.concat([X, y], axis = 1)
		return self.df
	
	def func(self):
		self.addGeoRelation()
		self.preprocess()
		self.process()

path = '/Users/sangtruong_2021/Desktop/' + 'short_2M_Simple_All.csv'
df = GinRummyDataset(path)

