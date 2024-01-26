# Summary
This is a homeproject to make something like google drive or onedrive.
So this is basically a "file sharing" backend software.

# Install
## Prerequirements:
- java 17 >=
- PostgreSQL 14 >=
- Linux (Windows is currently not fully supported)

## How to install
1. Get the .jar file using maven: First download the main branch into an IDE then use the command inside the IDE below:
```
mvn package -DskipTests
```
This command makes a .jar package out of the files.
2. Initialize the program:
Useable tags: 
- --init -> if set to true, then the program will initialize its self when any GET access point is being called. **CAUTION THIS IS A DESTRUCTIVE PROCESS**
- --my_root_directory -> The root directory can be set via this tag. This **should always be set** if the **init** tag was used with it.

Run the .jar file using --init=true tag, and if you want, you can define the home folder using the --my_root_directory=YOUR DIRECTORY tag
If you leave the root directory tag to it's default value, then either you should run the program in **sudo** OR you can make a **separate user** for it, and allow this user to **read/write** the **/opt** directory. (The latter is the proposed)

examples:
```
java -jar file_share-1.0.1 --init=true --my_root_directory=/opt/myOwnDirectory
java -jar file_share-1.0.1 --my_root_directory=/opt/myOwnDirectory
```
```
sudo java -jar file_share-1.0.1 --init=true
sudo java -jar file_share-1.0.1
```

3. Make an API call to the /api/get/info accesspoint without any extra attributes. This should initialize the directory structure.
4. Restart the program without the --init tag.
