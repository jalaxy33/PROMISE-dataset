#!/bin/sh

# Must be executed from current directory.

OUTDIR=../../build
mkdir -p $OUTDIR
CLASSPATH=../../lib/pbeans.jar
export CLASSPATH
javac -d "$OUTDIR" `find . -name '*.java'`
