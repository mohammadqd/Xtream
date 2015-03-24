#!/bin/bash
JVM_PARAMS="-Xmx512M -ea -server"
if [[ $# -ge 4 || $# -le 0 ]]; then
 echo "HELP: $0 NUMBER_OF_RUN [NAME_OF_EXPERIMENTS_FOLDER [XTREAM_JAR_NAME]]"
 echo "Example: $0 10 FLS Xtream2.jar"
exit 1;
else
echo "Running Xtream for $1 times..."
if [ $# -ge 2 ]; then
  mkdir EXPR_$2
fi
for (( c=0; c< $1 ; c++ ))
do
  if [ $# -eq 3 ]; then
	mkdir EXPR_$2/$c
	java $JVM_PARAMS -jar $3 $2 $c 2>&1 | tee EXPR_$2/$c/stdout_err.txt
#	~/jrockit/bin/java $JVM_PARAMS -jar $3 $2 $c 2>&1 | tee EXPR_$2/$c/stdout_err.txt
  fi
  if [ $# -eq 2 ]; then
	mkdir EXPR_$2/$c
	java $JVM_PARAMS -jar xtream.jar $2 $c 2>&1 | tee EXPR_$2/$c/stdout_err.txt
#	~/jrockit/bin/java $JVM_PARAMS -jar xtream.jar $2 $c 2>&1 | tee EXPR_$2/$c/stdout_err.txt
  fi 
  if [ $# -eq 1 ]; then
	mkdir output/$c
	java $JVM_PARAMS -jar xtream.jar $c 2>&1 | tee output/$c/stdout_err.txt
#	~/jrockit/bin/java $JVM_PARAMS -jar xtream.jar $c 2>&1 | tee output/$c/stdout_err.txt
  fi
  echo "[[[ WAITING TO START RUN: ($c+2) of $1... ]]]"
  sleep 5s 
done
echo "============================"
echo "         F   I   N          "
echo "============================"
fi
