java -javaagent:target/bigio-agent-1.1-SNAPSHOT.jar -D"io.bigio.pingpong.throwaway=5000" -cp "config;config/producer;target/*" io.bigio.benchmark.pingpong.PingPongProducer
