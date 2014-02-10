/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.io.IOException;
import java.util.Map;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author atrimble
 */
public abstract class AbstractRPCMessage implements RPCMessage {
    
    protected static Map<String, Value> decodeHeader(byte[] bytes, Unpacker unpacker) throws IOException {
        Template<Map<String, Value>> headerTemplate = Templates.tMap(Templates.TString, Templates.TValue);
        
        Map<String, Value> header = unpacker.read(headerTemplate);
        return header;
    }
}
