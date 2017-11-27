#!/usr/bin/env bash

if test "$1" == "debug"
then
    echo Enabling debug
    JAVA_OPTS=${JAVA_OPTS}-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=3333
fi

( mvn -DskipTests clean install && java $JAVA_OPTS -jar rockscript-server/target/rockscript.jar )
