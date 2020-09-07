# The features data don't seem to fit very well into this model.

# Import packages
import numpy as np
import matplotlib.pyplot as plt
import keras
from sklearn.model_selection import train_test_split
from keras.models import Sequential
from keras.layers import Conv2D, MaxPooling2D, Dense, Flatten

# Import features
X = genfromtxt(path + 'features.txt', delimiter=' ')

# Generate label from data of only simple player 0
label = open(path + "gamestates.txt", "r")
label = list(label)

i = 1
y = []
while i < len(label):
    gs_info = list(map(int, label[i].split()))
    if gs_info[3] == 0:
        local_y = np.ones(gs_info[0]) # If winner is 0: Append win
        for j in local_y: y.append(j)
    else:
        local_y = np.zeros(gs_info[0]) # If winner is 1: Append lose 
        for j in local_y: y.append(j)
    i += gs_info[0] + 1
y = np.array(y)
# y.reshape(39, -1)

# Data splitting
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.1, random_state = 42)

# Model configuration
model = Sequential([
  Conv2D(filters = 32, kernel_size = (3,3), input_shape = (5, 4, 13), padding = 'same', activation = 'sigmoid'), 
  Conv2D(filters = 40, kernel_size = (3,3), input_shape = (32, 4, 13), padding = 'same', activation = 'sigmoid'),
  Conv2D(filters = 46, kernel_size = (3,3), input_shape = (40, 4, 13), padding = 'same', activation = 'sigmoid'),
  MaxPooling2D(pool_size = (3,3)),

  Flatten(),
  Dense(1, activation = 'sigmoid'),
])

adam = keras.optimizers.Adam(learning_rate=0.01)
sgd = keras.optimizers.SGD(learning_rate=0.1, momentum=0.9)

model.compile(
  optimizer = adam,
  loss='binary_crossentropy',
  metrics=['accuracy'],
)

# Model optimization:
history = model.fit(X_train, y_train, epochs = 50, validation_data=(X_test, y_test))

# model.save(path + 'model100')

# list all data in history
# print(history.history.keys())

# summarize history for accuracy
plt.plot(history.history['val_accuracy'])
plt.title('model accuracy')
plt.ylabel('accuracy')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.show()

# summarize history for loss
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.show()