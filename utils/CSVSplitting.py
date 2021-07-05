import random
from google.colab import drive
drive.mount('/content/gdrive/')

path = 'gdrive/My Drive/Colab Notebooks/GR-TrainModel/Alucard/'

fin = open(path + '2M_Simple_All_noDup.csv', 'rb')
train = open(path + '2M_Simple_All_noDup_train.csv', 'wb')
test = open(path + '2M_Simple_All_noDup_test.csv', 'wb')

for line in fin:
    r = random.random()
    if r < 0.85:
        train.write(line)
    else:
        test.write(line)
fin.close()
train.close()
test.close()
