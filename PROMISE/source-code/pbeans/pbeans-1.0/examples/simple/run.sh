#!/bin/sh

# Run ./build.sh first
# Must be executed from current directory.

CLASSPATH=:../../build:../../lib/pbeans.jar:../../ext/mysql-connector-java-3.0.11-stable-bin.jar:../../ext/jtds-0.7.1.jar
case `uname` in
    CYGWIN*)
      CLASSPATH=`cygpath -p -w "$CLASSPATH"`
      ;;
    *)
      ;;
esac
export CLASSPATH
java net.sourceforge.pbeans.examples.Main "$@"
