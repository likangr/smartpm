#!/bin/bash
# get all filename in specified path
 
path=$1
files=$(ls $path)
for filename in $files
do
 
 filePath=$path/$filename
 
 echo cur file: $filePath
 
 java -jar /Users/likangren/Documents/joyshow-work/dev/AXMLPrinter2.S.jar $filePath
done
