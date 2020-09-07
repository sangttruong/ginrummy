import pandas as pd
import tensorflow as tf
from tensorflow.python.keras.models import Sequential
from tensorflow.python.keras.layers import Dense
from tensorflow.python.keras.callbacks import EarlyStopping
from sklearn.model_selection import train_test_split
path = '/home/LDAPdir/struong21/GR/GinRummy/' #Alucard

# load data
df = pd.read_csv(path + 'Processed_cumData.csv')
y = df.pop('score0_mean')
X_train, X_test, y_train, y_test = train_test_split(df.to_numpy(), y, test_size=0.15, random_state=42)

# define model: equivalent to a linear regression
def get_compiled_model():
  model = Sequential()
  model.add(Dense(1, input_dim=178))
  model.compile(loss='mean_squared_error', optimizer='adam')
  return model

model = get_compiled_model()
es = EarlyStopping(monitor='val_loss', verbose=1, patience=5, min_delta=1)
history = model.fit(X_train, y_train, validation_data = (X_test, y_test), epochs = 100, batch_size=8, callbacks = [es])

# serialize model to YAML
model_yaml = model.to_yaml()
with open(path + "NN1.yaml", "w") as yaml_file: yaml_file.write(model_yaml)

# serialize weights to HDF5
model.save_weights("NN1_weights.h5")
print("Saved model to disk")
