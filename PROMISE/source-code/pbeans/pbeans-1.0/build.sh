#!/bin/sh
mkdir -p ./build
mkdir -p ./lib
javac -d ./build `find ./src -name '*.java'`
(cd build; jar cvf ../lib/pbeans.jar *) 
