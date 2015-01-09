#!/bin/bash

java -javaagent:bin/bigio-agent-1.1.1-SNAPSHOT-cp bin/bigio-core-1.1.1-SNAPSHOT:config:lib/*:components/* io.bigio.Starter
