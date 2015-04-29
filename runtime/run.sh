#!/bin/bash

java -javaagent:bin/bigio-agent-1.1.2.jar -cp bin/bigio-core-1.1.2.jar:config:lib/*:components/* io.bigio.Starter
