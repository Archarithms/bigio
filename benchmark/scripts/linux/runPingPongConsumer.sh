#!/bin/sh

java -javaagent:../../target/bigio-agent-1.1-SNAPSHOT.jar -D"io.bigio.pingpong.throwaway=5000" -cp "../../config:../../config/consumer:../../target/*" io.bigio.benchmark.pingpong.PingPongConsumer
