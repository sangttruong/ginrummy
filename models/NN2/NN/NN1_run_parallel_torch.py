from torch.nn.init import xavier_uniform_
from torch.nn import Linear
from torch.nn import Module
import torch
from torch import Tensor

import numpy as np
import sys
import os
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')
from tensorflow.python.keras.models import model_from_yaml
sys.stderr = stderr

class MLP(Module):
    # define model elements
    def __init__(self, n_inputs):
        super(MLP, self).__init__()
        self.hidden1 = Linear(n_inputs, 1)
        xavier_uniform_(self.hidden1.weight)
        
    # forward propagate input
    def forward(self, X):
        X = self.hidden1(X)
        return X
   
dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/'
model = MLP(178)
model.load_state_dict(torch.load(dir + 'NN1_torch'))

features_file = open(dir + sys.argv[1], 'r')
features = features_file.readlines()

feed_features = []
for feature in features:
    feature_values = feature.split()
    feature_values = [int(i) for i in feature_values]
    feed_features.append(feature_values)

row = Tensor([feed_features])
yhat = model(row)
result = yhat.detach().numpy()
q_value_file = open(dir + 'q_value_' + sys.argv[1], 'w')
for i in range(len(features)):
    q_value_file.write(str(result[0][i][0]) + '\n')

q_value_file.close()
features_file.close()

