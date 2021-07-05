def getEDeck ():
  eDeck = ['e1A','e2A','e3A','e4A','e5A','e6A','e7A','e8A','e9A','e10A','e11A','e12A','e13A',
           'e1B','e2B','e3B','e4B','e5B','e6B','e7B','e8B','e9B','e10B','e11B','e12B','e13B',
           'e1C','e2C','e3C','e4C','e5C','e6C','e7C','e8C','e9C','e10C','e11C','e12C','e13C',
           'e1D','e2D','e3D','e4D','e5D','e6D','e7D','e8D','e9D','e10D','e11D','e12D','e13D']

  return eDeck

def getDeck ():
  Deck = ['1A','2A','3A','4A','5A','6A','7A','8A','9A','10A','11A','12A','13A',
           '1B','2B','3B','4B','5B','6B','7B','8B','9B','10B','11B','12B','13B',
           '1C','2C','3C','4C','5C','6C','7C','8C','9C','10C','11C','12C','13C',
           '1D','2D','3D','4D','5D','6D','7D','8D','9D','10D','11D','12D','13D']

  return Deck

data = pd.read_csv("gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/2M_Simple_All_OHE_noDup_ISRU.csv")
y = data['score0_mean']
f = data[['GamestateNum','RunNum0','SetNum0','Deadwood0','Hitscore0']]
f = f.to_numpy()
o = data[getEDeck()]
o = o.to_numpy()
x = data[getDeck()]
x = x.to_numpy()
x = x.reshape((-1,4,13,1)) # If use convolution operator
