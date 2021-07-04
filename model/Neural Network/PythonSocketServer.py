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
    message = clientsocket.recv(1024).decode() # Get a number from Java
    
    print("Input: " + message)    
    
    newMessage = np.array(ast.literal_eval(message))
    newMessage = newMessage + 2
    newMessage = str(newMessage.tolist())
    
    print("Output: " + newMessage)
    clientsocket.send(newMessage.encode()) # Send a number back to Java
    print("Computed and sent!")
    clientsocket.close()
