# **How to add a new Git Repository on Alucard**
TO-DO

# **How to send files from local computer to Alucard**

### On local machine: 
1) $ zip **file**
2) $ scp **directory-of-file-to-be-sent** struong21@alucard.csc.depauw.edu:/home/LDAPdir/struong21

### On Alucard:
1) $ cd **proper-working-directory**
2) $ unzip **file**

# **How to send files from Alucard to local computer**

TO-DO

# **How to send large files from Alucard to local computer**

### On Alucard: 
1) $ curl --upload-file ./**NameOfUploadFile** https://transfer.sh/ **NameOfUploadFile**

### On local machine: 
1) $ curl **httpsResult** -o **NameOfDownloadFile**

Reference: https://www.tecmint.com/file-sharing-from-linux-commandline/

# How to run a Java program on Alucard
1) $ rm \*.class
2) $ javac **fileName**.java
3) $ java **fileName**

# How to edit file with vi on Alucard
- Open file: vi **fileName**
- Quit: :q
- Quit and save: :wq
- Edit: i
- Exit edit mode: esc
- Jump to line: :lineNum

# How to fetch master on Alucard
1) $ git reset --hard
2) $ cd GinRummy
3) $ git branch (make sure it is on develop branch)
4) $ git fetch upstream
5) $ git merge upstream/master

# How to push to master on Alucard
1) $ cd **workingDirectory**
2) $ git status
3) $ git add . OR $ git add **fileDirectory**
4) $ git commit -m "stringMessage"
5) $ git push origin develop

# How to create pull request on Alucard
https://git-scm.com/docs/git-request-pull

# Pull request
1) On kylepham github page,
2) Pull requests >> New pull request >> compare: develop >> Create pull request

# Note 
1) command with '&': run in the background, so you need its ID to kill it (kill ID, or kill -9 ID)
2) command with nohup: run in the background and ignore hang-up
