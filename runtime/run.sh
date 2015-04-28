#!/bin/bash

java -javaagent:bin/bigio-agent-1.1.2-SNAPSHOT-cp bin/bigio-core-1.1.2-SNAPSHOT:config:lib/*:components/* io.bigio.Starter
