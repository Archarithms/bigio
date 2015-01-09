#!/bin/bash

java -javaagent:bin/bigio-agent-1.1-cp bin/bigio-core-1.1:config:lib/*:components/* io.bigio.Starter
