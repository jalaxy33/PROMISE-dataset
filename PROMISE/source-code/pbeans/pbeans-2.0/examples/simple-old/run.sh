#!/bin/sh

# Run ./build.sh first
# Must be executed from current directory.

CLASSPATH=:.:../../lib/pbeans.jar:../../ext/mysql-connector-java-5.0.5-bin.jar:../../ext/jtds-0.7.1.jar:../../ext/pg74jdbc3.jar
case `uname` in
    CYGWIN*)
      CLASSPATH=`cygpath -p -w "$CLASSPATH"`
      ;;
    *)
      ;;
esac
export CLASSPATH
java Main1 "$@"
