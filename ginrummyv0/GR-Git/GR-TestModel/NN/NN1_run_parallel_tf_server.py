import numpy as np

# https://github.com/keras-team/keras/issues/1406
# disable 'Using xxx backend'
import sys
import os
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')
from tensorflow.python.keras.models import model_from_yaml
sys.stderr = stderr

# disable any warnings by TensorFlow.
# https://stackoverflow.com/questions/47068709/your-cpu-supports-instructions-that-this-tensorflow-binary-was-not-compiled-to-u
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

name = 'NeuralNet_TF_0L'

#dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/' # For Alucard

dir = '/Users/sangtruong_2021/Documents/GitHub/GR-Git/GR-TestModel/NN/' # for direct run from python.
# load model from yaml
with open(dir + name + '.yaml', 'r') as file:
    model = model_from_yaml(file.read())
    file.close()

# load weights from h5 file to model
model.load_weights(dir + name + '.h5')


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
    features_list = features_list.reshape((1, 178))
    print(features_list.shape)
    result = model.predict(features_list)
    result = result.reshape((-1))
    newMessage = str(result.tolist())
    
    print("Output: " + newMessage)
    clientsocket.send(newMessage.encode()) # Send a number back to Java
    print("Computed and sent!")
    clientsocket.close()