/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak;

import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

/**
 *
 * @author atrimble
 */
@Component
public class Speaker {

    private static final Logger LOG = LoggerFactory.getLogger(Speaker.class);

    private final MessagePack msgPack = new MessagePack();

    @Message
    public static class SomeMessage {
        public String name;
        public double version;
    }

    public Speaker() {
        LOG.info("The speaker has arrived");

//        SomeMessage message = new SomeMessage();
//        message.name = "Speaker";
//        message.version = 1.0;
//        
//        try {
//            byte[] bytes = msgPack.write(message);
//
//            Envelope envelope = new Envelope();
//            envelope.type = SomeMessage.class.getName();
//            envelope.payload = bytes;
//
//            byte[] envBytes = msgPack.write(envelope);
//
//            LOG.info("Message serialized into " + envBytes.length + " bytes.");
//
//            Envelope s = msgPack.read(envBytes, Envelope.class);
//            LOG.info("Received message of type " + s.type);
//
//            SomeMessage m = (SomeMessage)msgPack.read(s.payload, Class.forName(s.type));
//
//            LOG.info(m.name + " v. " + m.version);
//        } catch (IOException ex) {
//            LOG.error("Exception serializing message.", ex);
//        } catch (ClassNotFoundException ex) {
//            LOG.error("Could not find message class.", ex);
//        }
//
//        Environment env = new Environment();
//        Reactor reactor = Reactors.reactor()
//                .env(env)
//                .dispatcher(Environment.EVENT_LOOP)
//                .get();
//
//        reactor.on(Selectors.object("parse"), new Consumer<Event<String>>() {
//            @Override
//            public void accept(Event<String> t) {
//                LOG.info("Received event with data: " + t.getData());
//            }
//        });
//
//        reactor.notify("parse", Event.wrap("data"));
    }
}
