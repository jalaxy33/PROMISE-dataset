#!/bin/sh

#   Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
#   reserved.

cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

REALANTHOME=$ANT_HOME
ANT_HOME=bootstrap
export ANT_HOME

if test ! -f bootstrap/lib/ant.jar -o  ! -x bootstrap/bin/ant -o ! -x bootstrap/bin/antRun ; then
  /bin/sh ./bootstrap.sh
fi    

if test ! -f bootstrap/lib/ant.jar -o  ! -x bootstrap/bin/ant -o ! -x bootstrap/bin/antRun ; then
  echo Bootstrap FAILED
  exit
fi

LOCALCLASSPATH=lib/xercesImpl.jar:lib/xml-apis.jar:bootstrap/lib/ant.jar
# add in the dependency .jar files
DIRLIBS=lib/optional/*.jar
for i in ${DIRLIBS}
do
    if [ "$i" != "${DIRLIBS}" ] ; then
        LOCALCLASSPATH=$LOCALCLASSPATH:"$i"
    fi
done

# make sure the classpath is in unix format
if $cygwin ; then
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`    
fi

CLASSPATH=$LOCALCLASSPATH:$CLASSPATH

# switch back to Windows format
if $cygwin ; then
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`    
fi

export CLASSPATH


if [ "$REALANTHOME" != "" ] ; then
  ANT_INSTALL="-Dant.install=$REALANTHOME"
fi

bootstrap/bin/ant -emacs $ANT_INSTALL $*

