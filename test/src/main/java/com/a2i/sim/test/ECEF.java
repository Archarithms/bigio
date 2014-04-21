/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import com.a2i.dms.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author atrimble
 */
@Message
public class ECEF implements Serializable {

//    private static final transient Logger LOG = LoggerFactory.getLogger(ECEF.class);
    private static final transient MessagePack msgPack = new MessagePack();

    private double x;
    private double y;
    private double z;

    public ECEF() {
        
    }

    public ECEF(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public byte[] test() {
        return new byte[0];
    }

    public Object decode(final Value value, final Class expectedType) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Object ret = null;

        switch(value.getType()) {
            case ARRAY:
                ret = new ArrayList();
                Value[] elements = value.asArrayValue().getElementArray();
                for(int i = 0; i < elements.length; ++i) {
                    ((List)ret).add(decode(elements[i], expectedType));
                }
                break;
            case BOOLEAN:
                ret = value.asBooleanValue().getBoolean();
                break;
            case FLOAT:
                if(expectedType == Float.class) {
                    ret = value.asFloatValue().getFloat();
                } else if(expectedType == Double.class) {
                    ret = value.asFloatValue().getDouble();
                }
                break;
            case INTEGER:
                if(expectedType == Integer.class) {
                    ret = value.asIntegerValue().getInt();
                } else if(expectedType == Long.class) {
                    ret = value.asIntegerValue().getLong();
                } else if(expectedType == Byte.class) {
                    ret = value.asIntegerValue().getByte();
                } else if(expectedType == Short.class) {
                    ret = value.asIntegerValue().getShort();
                }
                break;
            case MAP:
                ret = new HashMap();
                Set<Value> keys = value.asMapValue().keySet();
                for(Value k : keys) {
                    Value v = value.asMapValue().get(k);
                    ((Map)ret).put(decode(k, String.class), decode(v, expectedType));
                }
                break;
            case RAW:
                if(expectedType == String.class) {
                    ret = value.asRawValue().getString();
                } else {
                    ret = expectedType.newInstance();
                    expectedType.getMethod("decode", byte[].class).invoke(ret, value.asRawValue().getByteArray());
                }
                break;
            case NIL:
            default:
                throw new IOException("Cannot decode message");
        }

        return ret;
    }

    public void decode(byte[] bytes) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        x = (double)decode(unpacker.readValue(), Double.class);
        y = (double)decode(unpacker.readValue(), Double.class);
        z = (double)decode(unpacker.readValue(), Double.class);
    }

    public byte[] encode() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Packer packer = msgPack.createPacker(out);

        packer.write(x);
        packer.write(y);
        packer.write(z);

        return out.toByteArray();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ECEF) {
            ECEF that = (ECEF)obj; 
            return x == that.getX() && y == that.getY() && z == that.getZ();
        }

        return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(double z) {
        this.z = z;
    }
}
