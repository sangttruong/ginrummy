def NeuralNet_TF_0L():
  model = Sequential()
  model.add(Dense(1, input_dim = 52))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_1L():
  model = Sequential()
  model.add(Dense(300, input_dim = 227, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00002) ))
  model.add(Dense(1))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_2L():
  model = Sequential()
  model.add(Dense(300, input_dim = 227, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001) ))
  model.add(Dense(300, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001)))
  model.add(Dense(1))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_3L():
  model = Sequential()
  model.add(Dense(300, input_dim = 227, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001) ))
  model.add(Dense(300, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001)))
  model.add(Dense(300, activation = 'sigmoid', kernel_regularizer = tf.keras.regularizers.l2(0.00001)))
  model.add(Dense(1))
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def NeuralNet_TF_Functional_0L():
  x = Input(shape = (1,4,13,))
  f = Input(shape = (5,))
  o = Input(shape = (52,))

  i = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first')(x)
  i = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first')(i)
  i = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first')(i)

  i = Flatten()(i)
  
  i = concatenate( [i, f, o])

  i = Dense(256, activation = 'relu')(i)
  i = Dense(256, activation = 'relu')(i)
  i = Dense(256, activation = 'relu')(i)

  y = Dense(1)(i)
  
  model = Model(inputs = [x, f, o], outputs = y)
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model

def Inception():
  x = Input(shape = (1,4,13,), name='x')
  f = Input(shape = (5,), name = 'f')
  o = Input(shape = (52,), name = 'o')

  i2 = Conv2D(filters = 64, kernel_size = 2, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first', name = 'i21')(x)
  i2 = Conv2D(filters = 64, kernel_size = 2, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first', name = 'i22')(i2)
  i2 = Flatten(name = 'i2flatten')(i2)

  i3 = Conv2D(filters = 64, kernel_size = 3, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first', name ='i31')(x)
  i3 = Conv2D(filters = 64, kernel_size = 3, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first', name = 'i32')(i3)
  i3 = Flatten(name = 'i3flatten')(i3)

  i4 = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first', name = 'i41')(x)
  i4 = Conv2D(filters = 64, kernel_size = 4, strides = 1, padding = 'same', activation = 'relu', data_format = 'channels_first', name = 'i42')(i4)
  i4 = Flatten(name = 'i4flatten')(i4)
  
  i = concatenate([i2, i3, i4], name = 'concat')

  i = Dense(256, activation = 'relu', name = 'dense1')(i)
  i = Dense(256, activation = 'relu', name = 'dense2')(i)
  i = Dense(256, activation = 'relu', name = 'dense3')(i)

  y = Dense(1, name = 'y')(i)
  
  model = Model(inputs = [x, f, o], outputs = y)
  model.compile(loss = 'mean_squared_error', optimizer = 'adam')
  return model
