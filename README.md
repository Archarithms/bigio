Checkout the [NodeJS](https://github.com/Archarithms/bigio-node) version of BigIO.

[Python](https://github.com/Archarithms/bigio-python) and [Go](https://github.com/Archarithms/bigio-go) 
versions of BigIO coming soon.

Note: The API has changed with version 1.2 and is not backwards compatible. io.bigio.Speaker
has been renamed to io.bigio.BigIO and the boostrap method has been moved into this class.
All instances of `Speaker speaker = Starter.boostrap();` need to be replaced with 
`BigIO bigio = BigIO.bootstrap();`

### BigIO

BigIO is a fast, distributed messaging framework written entirely in Java. The 
framework can run in a single process or across multiple instances running 
across a network. Message communication in-VM is extremely fast with upwards of 
4.6 million messages per second. Communication across multiple instances on a 
network happens via TCP or UDP connections. Even across the network, the 
framework can process around 300,000 messages per second (150,000 messages sent 
and 150,000 messages received).

One of the newest features is the ability to send and receive fully encrypted
messages and/or run over TLS/SSL.  The ability to do secure messaging sets
BigIO apart from other middleware such as DDS.

To get started using BigIO, download the BigIO runtime, and deploy your jar
to the 'components' directory. The API can be used by adding the following to
your Maven pom.

```XML
<dependency>
    <groupId>io.bigio</groupId>
    <artifactId>bigio-core</artifactId>
    <version>1.2</version>
</dependency>
```

A zipped runtime package can be downloaded [here](http://search.maven.org/remotecontent?filepath=io/bigio/bigio-runtime/1.2/bigio-runtime-1.2.zip)

For more information, please see the [wiki](https://github.com/Archarithms/bigio/wiki)
or our [website](http://bigio.io)

The Javadocs can be viewed [here](http://bigio.io/javadoc/)

### What's New

##### 1.2
- New multicast discovery implementation using NIO
- API enhancements - better naming
- Several discovery bugs fixed
- Improved memory usage

##### 1.1.3
- Fixed a Mac specific discovery bug
- Removed logging for a cleaner interface

##### 1.1.2
- Interoperability with the NodeJS BigIO
- Fixed a bug when sending empty messages

##### 1.1.1
- Fixed an initialization bug

##### 1.1
- Encrypted messaging
- SSL/TLS messaging
- Java 8 support
- Faster message serialization
- Assorted bug fixes

##### 1.0.2
- Ability to suppress message forwarding

##### 1.0.1
- Bug fixes
