#!/bin/sh
#set -x

###########################
## Shell script to compile the chat server
###########################


echo "Running compilation"

# Compile 
javac cs4400chatserver/main/java/*.java

echo "Done"