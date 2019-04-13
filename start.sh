#!/bin/sh
rm -f tpid
nohup java -jar target/bim-0.0.1-SNAPSHOT.jar --spring.profiles.active=stg > /dev/null 2>&1 &
echo $! > tpid