import numpy as np

# https://github.com/keras-team/keras/issues/1406
# disable 'Using xxx backend'
import sys
import os
stderr = sys.stderr
sys.stderr = open(os.devnull, 'w')
from keras.models import model_from_yaml
sys.stderr = stderr

# disable any warnings by TensorFlow. 
# https://stackoverflow.com/questions/47068709/your-cpu-supports-instructions-that-this-tensorflow-binary-was-not-compiled-to-u
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

dir = '/Users/hoangpham/' # for direct run from python.
# load model from yaml
with open('Documents/Java/gin-rummy-eaai-master/Model/model.yaml', 'r') as file:
    model = model_from_yaml(file.read())
    file.close()

# load weights from h5 file to model
model.load_weights('Documents/Java/gin-rummy-eaai-master/Model/model.h5')

features_file = open('Documents/Java/gin-rummy-eaai-master/Model/features.txt', 'r')
features = features_file.readlines()

discount = 0.1
X = []
for feature in features[:-1]:
    feature = feature.split()
    feature = [int(x) for x in feature]
    X.append(feature)

Y = model.predict(np.array(X))
y = [int(features[-1]) + discount * _y[0] for _y in Y]

model.fit(X, y, batch_size=1, epochs=1, verbose=0)

features_file.close()






