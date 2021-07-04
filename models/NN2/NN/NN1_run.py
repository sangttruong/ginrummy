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

dir = '/home/LDAPdir/struong21/GR/GinRummy/GR-TestModel/NN/' # for direct run from python.
# load model from yaml
with open(dir + 'NN1.yaml', 'r') as file:
    model = model_from_yaml(file.read())
    file.close()

# load weights from h5 file to model
model.load_weights(dir+'NN1_weights.h5')

features_file = open(dir+'features.txt', 'r')
features = features_file.readlines()


feed_features = []
for feature in features:
    feature_values = feature.split()
    feature_values = [int(i) for i in feature_values]
    feed_features.append(feature_values)

# print(feed_features)
result = model.predict(np.array(feed_features))
# print(result)

# Cant' have Users/sangtruong_2021
q_value_file = open(dir+'q_value.txt', 'w')
for i in range(len(features)):
    q_value_file.write(str(result[i][0]) + '\n')

q_value_file.close()
features_file.close()
