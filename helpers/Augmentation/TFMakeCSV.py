!head {train_file_path}
CSV_COLUMNS = ['1A','2A','3A','4A','5A','6A','7A','8A','9A','10A','11A','12A','13A',
               '1B','2B','3B','4B','5B','6B','7B','8B','9B','10B','11B','12B','13B',
               '1C','2C','3C','4C','5C','6C','7C','8C','9C','10C','11C','12C','13C',
               '1D','2D','3D','4D','5D','6D','7D','8D','9D','10D','11D','12D','13D',
               'GamestateNum','RunNum0','SetNum0','Deadwood0','Hitscore0','score0_mean']
LABEL_COLUMN = 'score0_mean'
def get_dataset(file_path):
  dataset = tf.data.experimental.make_csv_dataset(
    file_path,
    batch_size=1, 
    label_name=LABEL_COLUMN,
    na_value="?",
    num_epochs=1,
    ignore_errors=True)
  return dataset

raw_train_data = get_dataset(train_file_path)
# raw_test_data = get_dataset(test_file_path)
examples, labels = next(iter(raw_train_data)) # Just the first batch.
print("EXAMPLES: \n", examples, "\n")
print("LABELS: \n", labels)
