#/bin/bash

java -javaagent:bin/bigio-agent-1.0-SNAPSHOT.jar -cp bin/bigio-core-1.0-SNAPSHOT.jar:config:lib/*:components/* io.bigio.Starter
