This is a homeproject to make something like google drive or onedrive.
So this is basically a "file sharing" backend software.

# Install
## Prerequirements:
- java 17 >=
- PostgreSQL 14 >=
- Linux (Windows is currently not fully supported)

## How to install
1. Get the .jar file using maven: Download the main branch and use the command below:
```
mvn package -DskipTests
```
2. Initialize the program:
Run the .jar file using --init=true tag, and if you want you can define the home folder using the --my_root_directory=YOUR DIRECTORY tag
If you leave the root directory tag to it's default value, then either you should run the program in sudo OR you can make a separate user for it, and allow this user to read/write the /opt directory. (The latter is the proposed)
3. Make an API call to the /api/get/info acccesspoint without any extra attributes. This should initialize the directory structure.
4. Restart the program without the --init tag.
