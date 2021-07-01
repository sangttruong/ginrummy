##Python 3
import urllib.request
url = "https://github.com/sangttruong/incomevis/raw/gh-pages/src/input/ipums-cps-lite2.gz"
print ("download start!")
filename, headers = urllib.request.urlretrieve(url, filename="ipums-cps-lite2.gz")
print ("download complete!")
print ("download file location: ", filename)
print ("download headers: ", headers)
