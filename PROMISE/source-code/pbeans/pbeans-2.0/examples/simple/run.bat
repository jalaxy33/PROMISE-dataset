@echo off

set CLASSPATH=".;..\..\lib\pbeans.jar;..\..\ext\mysql-connector-java-3.0.11-stable-bin.jar;..\..\ext\jtds-0.7.1.jar;..\..\ext\pg74jdbc3.jar"
java Main %1 %2 %3
