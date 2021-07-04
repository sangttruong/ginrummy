import numpy as np
import sys
import os
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')
from tensorflow.python.keras.models import model_from_yaml
sys.stderr = stderr

# disable any warnings by TensorFlow.
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/'

# load model from yaml
with open(dir + '54.yaml', 'r') as file:
    model = model_from_yaml(file.read())
    file.close()

# load weights from h5 file to model
model.load_weights(dir + '54.h5')

print(model.summary())

import socket
import ast

listensocket = socket.socket()
listensocket.bind(("127.0.0.1", 8888))
listensocket.listen(999999999)
print("Server started at 127.0.0.1 on port " + str(8888))

running = True
while running:
    (clientsocket, address) = listensocket.accept()
#    print("New connection make!")
    features_list = clientsocket.recv(1024).decode() # Get a number from Java
    
#    print("Input: " + features_list)
    
    features_list = np.array(ast.literal_eval(features_list))
    features_list = features_list.reshape((1, 178))
#    print(features_list.shape)
    result = model.predict(features_list)
    result = result.reshape((-1))
    newMessage = str(result.tolist())
    
#    print("Output: " + newMessage)
    clientsocket.send(newMessage.encode()) # Send a number back to Java
#    print("Computed and sent!")
    clientsocket.close()
