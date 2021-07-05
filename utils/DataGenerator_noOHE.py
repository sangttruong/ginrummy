class DataGenerator_noOHE(tf.keras.utils.Sequence):
  'Generates data for tf.keras'
  def __init__(self, batch_size = 64, shuffle = True, datasettype = 'train', permFreq = 0.15):
    'Initialization'
    self.datasettype = datasettype
    if datasettype == 'train': 
        self.link = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/2M_Simple_All_noDup_train.csv'
    elif datasettype == 'valid':
        self.link = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/2M_Simple_All_noDup_test.csv'
    else: 
        print("Incorrect datatype")
    self.xy = np.loadtxt(self.link, delimiter = ",", dtype = np.float32, skiprows = 1)
    self.x = self.xy[:, 0:52] # exclude 52...
    self.x = self.x.reshape((-1,4,13,1))
    self.f = self.xy[:, 52:57] #exclude 57 ...
    self.y = self.xy[:, [57]] #to keep the format as numpy array
    self.dim = (4,13)
    self.batch_size = batch_size
    self.shuffle = shuffle
    self.indexes = np.arange(self.xy.shape[0])
    self.on_epoch_end()
    self.permFreq = permFreq

  def __len__(self):
    'Denotes the number of batches per epoch'
    return int(np.floor(self.xy.shape[0] / self.batch_size))

  def __getitem__(self, index_batch):
    'Generate one batch of data'
    # Generate indexes of the batch
    indexes_batch = self.indexes[index_batch*self.batch_size:(index_batch+1)*self.batch_size]    
    # Generate data
    [x, f], y = self.__data_generation(indexes_batch)

    return [x, f], y

  def on_epoch_end(self):
    'Updates indexes after each epoch'
    if self.shuffle == True:
      np.random.shuffle(self.indexes)

  def __data_generation(self, indexes):
    'Generates data containing batch_size samples' # X : (n_samples, *dim, n_channels)
    # Initialization
    x = np.empty((self.batch_size, 4,13,1))
    f = np.empty((self.batch_size, 5))
    y = np.empty((self.batch_size))

    # Generate data
    for i, index in enumerate(indexes):
      r = random.random()
      if r < (1-self.permFreq):
        x[i,] = self.x[index,]
        f[i,] = self.f[index,]
        y[i ] = self.y[index,]
      else: 
        x[i,] = np.array([np.random.permutation(self.x[index,0,])])
        f[i,] = self.f[index,]
        y[i ] = self.y[index,]

    return [x, f], y
