#/bin/bash

java -javaagent:bin/dms-agent-1.0-SNAPSHOT.jar -cp bin/dms-core-1.0-SNAPSHOT.jar:config:lib/*:components/* com.a2i.sim.Starter interactive
