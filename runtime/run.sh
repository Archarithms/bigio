#/bin/bash

java -javaagent:bin/a2isim-core-1.0-SNAPSHOT.jar -cp bin/a2isim-core-1.0-SNAPSHOT.jar:config:lib/*:components/* com.a2i.sim.Starter interactive
