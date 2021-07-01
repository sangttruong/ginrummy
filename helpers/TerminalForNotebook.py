# How to cd to directory
from google.colab import drive
drive.mount('/content/gdrive/')
%cd gdrive
%cd My\ Drive
%cd Colab\ Notebooks
%cd GR-TrainModel/Alucard/

# See the head of file
!head {train_file_path}
