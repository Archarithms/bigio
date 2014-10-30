#!/bin/sh

java -javaagent:../../target/bigio-agent-1.1-SNAPSHOT.jar -cp "../..config:../../config/consumer:../../target/*" io.bigio.benchmark.pingpong.PingPongVM
