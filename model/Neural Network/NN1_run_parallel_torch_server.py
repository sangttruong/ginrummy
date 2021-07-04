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
   
#dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/' # For Alucard
dir = '/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-TestModel/NN/'
model = MLP(178)
model.load_state_dict(torch.load(dir + 'NN1_torch'))

import socket
import numpy as np
import ast

listensocket = socket.socket()
listensocket.bind(("127.0.0.1", 8000))
listensocket.listen(999)
print("Server started at 127.0.0.1 on port " + str(8000))

running = True
while running:
    (clientsocket, address) = listensocket.accept()
    print("New connection make!")
    features_list = clientsocket.recv(1024).decode() # Get a number from Java
    
    print("Input: " + features_list)
    
    features_list = np.array(ast.literal_eval(features_list))
    rows = Tensor([features_list])
    yhat = model(rows)
    result = yhat.detach().numpy()
    result = result.reshape((-1))
    newMessage = str(result.tolist())
    
    print("Output: " + newMessage)
    clientsocket.send(newMessage.encode()) # Send a number back to Java
    print("Computed and sent!")
    clientsocket.close()
