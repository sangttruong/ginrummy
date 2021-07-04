# Create dataset 
class Simple_All_OHE_noDup_ISRU(Dataset):
  def __init__ (self, transform = None):
    # data loading
    xy = np.loadtxt('gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/2M_Simple_All_OHE_noDup_ISRU.csv', delimiter = ",", dtype = np.float32, skiprows = 1)
    self.x = xy[:, 0:52] # exclude 52...
    self.x = self.x.reshape((-1,1,4,13))
    self.f = xy[:, 52:57] #exclude 57 ...
    self.o = xy[:, 57:109] #excluding 109 ...
    self.y = xy[:, [109]] #to keep the format as numpy array
    self.n_samples = xy.shape[0]
    self.transform = transform

  def __getitem__(self, index):
    # dataset[0]
    sample = self.x[index], self.f[index], self.o[index], self.y[index]
    if self.transform:
      sample = self.transform(sample)
    return sample

  def __len__(self):
    #len(data)
    return self.n_samples

  def get_splits(self, n_test=0.15):
    test_size = round(n_test * self.n_samples)
    train_size = self.n_samples - test_size
    return random_split(self, [train_size, test_size])

# Data augmentation
class ToTensor:
  def __call__(self, sample):
    x, f, o, y = sample
    return torch.from_numpy(x), torch.from_numpy(f), torch.from_numpy(o), torch.from_numpy(y)

class Permutation:
  def __call__(self, sample):
    x, f, o, y = sample
    x = np.array([np.random.permutation(x[0])])
    # f = np.random.permutation(f)
    return x, f, o, y
