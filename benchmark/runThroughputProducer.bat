java -javaagent:target/bigio-agent-1.1-SNAPSHOT.jar -Dio.bigio.noJavassist=false -cp "config;target/*" io.bigio.test.throughput.ThroughputProducer
