Fast, distributed messaging in Java.

### BigIO

BigIO is a fast, distributed messaging framework written entirely in Java. The 
framework can run in a single process or across multiple instances running 
across a network. Message communication in-VM is extremely fast with upwards of 
4.6 million messages per second. Communication across multiple instances on a 
network happens via TCP or UDP connections. Even across the network, the 
framework can process around 300,000 messages per second (150,000 messages sent 
and 150,000 messages received).

To get started using BigIO, download the BigIO runtime, and deploy your jar
to the 'components' directory. The API can be used by adding the following to
your Maven pom.

```XML
<dependency>
    <groupId>io.bigio</groupId>
    <artifactId>bigio-core</artifactId>
    <version>1.0</version>
</dependency>
```

A zipped runtime package can be downloaded [here] (http://search.maven.org/remotecontent?filepath=io/bigio/bigio-runtime/1.0/bigio-runtime-1.0.zip)

For more information, please see our [wiki](https://github.com/Archarithms/bigio/wiki)
