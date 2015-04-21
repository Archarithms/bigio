/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

var winston = require('winston')
var logger = new (winston.Logger)({
    transports: [
        new (winston.transports.Console)({ level: 'debug' })
        //new (winston.transports.File)({ filename: 'somefile.log' })
    ]
});
var events = require('events');
var MemberStatus = require('./member-status');
var parameters = require('../parameters');
var ListenerRegistry = require('./listener-registry');
var EnvelopeDecoder = require('../codec/envelope-decoder');
var GossipDecoder = require('../codec/gossip-decoder');
var GenericDecoder = require('../codec/generic-decoder');

var gossipReactor = new events.EventEmitter();

var MeMember = function(ip, gossipPort, dataPort, useTCP) {

    this.ip = ip;
    this.dataPort = dataPort;
    this.gossipPort = gossipPort;
    this.useTCP = useTCP;

    var SSL_PROPERTY = "io.bigio.ssl";
    var DEFAULT_SSL = false;
    var SSL_SELFSIGNED_PROPERTY = "io.bigio.ssl.selfSigned";
    var DEFAULT_SELFSIGNED = true;
    var SSL_CERTCHAINFILE_PROPERTY = "io.bigio.ssl.certChainFile";
    var DEFAULT_CERTCHAINFILE = "conf/certChain.pem";
    var SSL_KEYFILE_PROPERTY = "io.bigio.ssl.keyFile";
    var DEFAULT_KEYFILE = "conf/keyfile.pem";
    var SSL_KEYPASSWORD_PROPERTY = "io.bigio.ssl.keyPassword";

    var ENCRYPTION_PROPERTY = "io.bigio.encryption";
    var DEFAULT_ENCRYPTION = false;

    var GOSSIP_TOPIC = "__gossiper";
    var DECODE_TOPIC = "__decoder";

    var symmetricCipher = undefined;
    var rsaCipher = undefined;
    var keyPair = undefined;

    var useEncryption = parameters.getInstance().getProperty(ENCRYPTION_PROPERTY, DEFAULT_ENCRYPTION);
    var useSSL = parameters.getInstance().getProperty(SSL_PROPERTY, DEFAULT_SSL);
    var useSelfSigned = parameters.getInstance().getProperty(SSL_SELFSIGNED_PROPERTY, DEFAULT_SELFSIGNED);
    var certChainFile = parameters.getInstance().getProperty(SSL_CERTCHAINFILE_PROPERTY, DEFAULT_CERTCHAINFILE);
    var keyFile = parameters.getInstance().getProperty(SSL_KEYFILE_PROPERTY, DEFAULT_KEYFILE);
    var keyPassword = parameters.getInstance().getProperty(SSL_KEYPASSWORD_PROPERTY);

    var gossipServer;
    var dataServer;
};

MeMember.prototype.tags = {};
MeMember.prototype.sequence = 0;
MeMember.prototype.status = MemberStatus.Unknown;
MeMember.prototype.ip = '';
MeMember.prototype.dataPort = -1;
MeMember.prototype.gossipPort = -1;
MeMember.prototype.useTCP = true;
MeMember.prototype.publicKey = undefined;

MeMember.prototype.toString = function() {
    var ret = "\nMember ";
    ret += this.ip;
    ret += ":";
    ret += this.gossipPort;
    ret += ":";
    ret += this.dataPort;
    if (this.status == MemberStatus.Alive || this.status == MemberStatus.Unknown) {
        ret += "\n    is ";
    } else {
        ret += "\n    has ";
    }
    ret += this.status;

    ret += "\n    with properties";
    for (var key in this.tags) {
        ret += "\n        ";
        ret += key;
        ret += " -> ";
        ret += this.tags[key];
    }

    ret += "\n";

    return ret;
};

MeMember.prototype.equals = function(obj) {
    var them = obj;

    return them != undefined
        && them.ip == this.ip
        && them.gossipPort == this.gossipPort
        && them.dataPort == this.dataPort;
};

MeMember.prototype.shutdown = function() {
    this.gossipServer.close();
    this.dataServer.close();
};

MeMember.prototype.initialize = function() {
    logger.debug("Initializing gossip server on " + this.ip + ":" + this.gossipPort);

    if (this.useSSL) {
        logger.info("Using SSL/TLS.");

        /* if(useSelfSigned) {
         logger.warn("Using self signed certificate. Only use this for testing.");
         SelfSignedCertificate ssc;
         try {
         ssc = new SelfSignedCertificate();
         sslContext = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
         } catch (CertificateException ex) {
         logger.error("Certificate error.", ex);
         } catch (SSLException ex) {
         logger.error("SSL error.", ex);
         }
         } else {
         try {
         if("".equals(keyPassword) || keyPassword == null) {
         sslContext = SslContext.newServerContext(SslProvider.JDK, new File(certChainFile), new File(keyFile));
         } else {
         sslContext = SslContext.newServerContext(SslProvider.JDK, new File(certChainFile), new File(keyFile), keyPassword);
         }
         } catch (SSLException ex) {
         logger.error("SSL error.", ex);
         }
         } */
    } else if(this.useTCP) {
        var net = require('net');

        var self = this;

        this.gossipServer = net.createServer(function(sock) {
            logger.debug('TCP gossip server connected');

            sock.on('end', function() {
                logger.debug('TCP gossip server disconnected');
            });

            sock.on('error', function(err) {

            });

            sock.on('data', function(data) {
                var message = GossipDecoder.decode(data);
                gossipReactor.emit('gossip', message);
            });
        }).listen(this.gossipPort, '0.0.0.0');

        this.dataServer = net.createServer(function(sock) {
            logger.debug('TCP data client connected');

            sock.on('end', function() {
                logger.debug('TCP data client disconnected');
            });

            sock.on('error', function(err) {

            });

            sock.on('data', function(data) {
                var message = EnvelopeDecoder.decode(data);
                message.decoded = false;
                self.send(message);
            });
        }).listen(this.dataPort, '0.0.0.0');

        /* var gossipClient = net.connect({port: this.gossipPort, host: this.ip}, function () {
            logger.debug('TCP gossip client connected');
        });
        gossipClient.on('end', function () {
            logger.debug('TCP gossip client disconnected');
        });
        gossipClient.on('data', function (data) {
            var message = GossipDecoder.decode(data);
            gossipReactor.emit('gossip', message);
        }); */

        /* var dataClient = net.connect({port: this.dataPort, host: this.ip}, function () {
            logger.debug('TCP data client connected');
        });
        dataClient.on('end', function () {
            logger.debug('TCP data client disconnected');
        });
        dataClient.on('data', function (data) {
            message = EnvelopeDecoder.decode(data);
            message.decoded = false;
            send(message);
        }); */
    } else {
        var dgram = require('dgram');

        var self = this;

        this.gossipServer = dgram.createSocket('udp4');

        this.gossipServer.on('listening', function () {
            logger.debug('UDP gossip server connected');
        });
        this.gossipServer.on('end', function () {
            logger.debug('UDP gossip server disconnected');
        });
        this.gossipServer.on('error', function (err) {

        });
        this.gossipServer.on('message', function (msg, rinfo) {
            var message = GossipDecoder.decode(msg);
            gossipReactor.emit('gossip', message);
        });

        this.gossipServer.bind(this.gossipPort, this.ip);

        this.dataServer = dgram.createSocket('udp4');

        this.dataServer.on('listening', function () {
            logger.debug('UDP data server connected');
        });
        this.dataServer.on('end', function () {
            logger.debug('UDP data server disconnected');
        });
        this.dataServer.on('error', function (err) {

        });
        this.dataServer.on('message', function (data, rinfo) {
            var message = envelopedecoder.decode(data);
            message.decoded = false;
            self.send(message);
        });

        this.dataServer.bind(this.dataPort, this.ip);
    }

    if(this.useEncryption) {
        logger.info("Requiring encrypted message traffic.");
        //KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        //keyGen.initialize(2048);
        //this.keyPair = keyGen.generateKeyPair();
        //this.publicKey = keyPair.getPublic().getEncoded();
        //this.symmetricCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //this.rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    }
};

MeMember.prototype.addGossipConsumer = function(consumer) {
    gossipReactor.addListener('gossip', consumer);
}

MeMember.prototype.send = function(envelope) {
    if(!envelope.decoded) {
        if(envelope.encrypted) {
            /*
            byte[] symKey;
            SecretKey key = null;
            try {
                // decrypt symmetric key
                synchronized(rsaCipher) {
                    rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                    symKey = rsaCipher.doFinal(envelope.getKey());
                }
                key = new SecretKeySpec(symKey, 0, symKey.length, "AES");
            } catch (IllegalBlockSizeException ex) {
                logger.error("Illegal block size in secret key.", ex);
            } catch (BadPaddingException ex) {
                logger.error("Bad padding in secret key.", ex);
            } catch (InvalidKeyException ex) {
                logger.error("Invalid public key.", ex);
            }

            try {
                // decrypt data with symmetric key
                byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                synchronized(symmetricCipher) {
                    symmetricCipher.init(Cipher.DECRYPT_MODE, key, ivspec);
                    envelope.setPayload(symmetricCipher.doFinal(envelope.getPayload()));
                }
            } catch (IllegalBlockSizeException ex) {
                logger.error("Private key too big.", ex);
            } catch (BadPaddingException ex) {
                logger.error("Bad padding in payload.", ex);
            } catch (InvalidKeyException ex) {
                logger.error("Invalid symmetric key.", ex);
            } catch (InvalidAlgorithmParameterException ex) {
                logger.error("Invalid algorithm.", ex);
            }
            */
        }

        // decode message
        envelope.message = GenericDecoder.decode(envelope.payload);
        //envelope.message = envelope.payload;
        envelope.decoded = true;
    }

    ListenerRegistry.send(envelope);
};

module.exports = MeMember;
