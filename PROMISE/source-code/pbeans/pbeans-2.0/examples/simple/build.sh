#!/bin/sh

# Must be executed from current directory.

CLASSPATH=../../lib/pbeans.jar
export CLASSPATH
javac -d . `find . -name '*.java'`
