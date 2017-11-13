#!/bin/sh
#set -x

###########################
## Shell script to compile the chat server
###########################


echo "Running compilation"

# Compile 
javac -cp src/main/java/com/company/*.java

echo "Done"