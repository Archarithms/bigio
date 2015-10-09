#!/bin/bash

java -javaagent:bin/bigio-agent-1.2.jar -cp bin/bigio-core-1.2:config:lib/*:components/* io.bigio.BigIO
