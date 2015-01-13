/**
 * Created by atrimble on 11/14/2014.
 */

var winston = require('winston');
var logger = new (winston.Logger)({
    transports: [
        new (winston.transports.Console)({ level: 'info' })
        //new (winston.transports.File)({ filename: 'msgpack.log' })
    ]
});

exports.encode = function (value, debug) {
    var size = 0;
    for(var key in value) {
        size += sizeof(value[key]);
    }

    if (size === 0) return undefined;
    var buffer = new Buffer(size);

    if (debug) {
        logger.info('Buffer size: ' + size);
        logger.info('Actual size: ' + buffer.size);
    }

    var offset = 0;
    for(var key in value) {
        if(debug) {
            logger.info('Encoding ' + key + ' --> ' + value[key]);
        }
        offset += encode(value[key], buffer, offset);
        if(debug) {
            logger.info('Offset: ' + offset);
            logger.info('buffer: ' + buffer.length);
        }
    }

    if (debug) {
        logger.info(buffer.toString('hex'));
    }

    //encode(value, buffer, 0);

    return buffer;
};

exports.decode = decode;

function Decoder(buffer, offset) {
    this.offset = offset || 0;
    this.buffer = buffer;
};

Decoder.prototype.map = function (length, debug) {
    var value = {};
    for (var i = 0; i < length; i++) {
        var key = this.parse(debug);
        value[key] = this.parse(debug);
    }
    return value;
};

Decoder.prototype.buf = function (length) {
    var value = this.buffer.slice(this.offset, this.offset + length);
    this.offset += length;
    return value;
};

Decoder.prototype.raw = function (length) {
    var value = this.buffer.slice(this.offset, this.offset + length).toString();
    this.offset += length;
    return value;
};

Decoder.prototype.array = function (length, debug) {
    var value = new Array(length);
    for (var i = 0; i < length; i++) {
        value[i] = this.parse(debug);
    }
    return value;
};

Decoder.prototype.parse = function (debug) {
    var type = this.buffer[this.offset];
    var value, length;
    // Fixed string
    if ((type & 0xe0) === 0xa0) {
        length = type & 0x1f;
        if(debug) {
            logger.info('Parsing fixed string of length ' + length + ' type code: ' + type.toString(16));
        }
        this.offset++;
        var raw = this.buf(length);
        return raw;
    }
    // FixMap
    if ((type & 0xf0) === 0x80) {
        if(debug) {
            logger.info('Parsing fixed map');
        }
        length = type & 0x0f;
        this.offset++;
        return this.map(length, debug);
    }
    // FixArray
    if ((type & 0xf0) === 0x90) {
        if(debug) {
            logger.info('Parsing fixed array');
        }
        length = type & 0x0f;
        this.offset++;
        return this.array(length, debug);
    }
    // Positive FixNum
    if ((type & 0x80) === 0x00) {
        if(debug) {
            logger.info('Positive fixed num');
        }
        this.offset++;
        return type;
    }
    // Negative Fixnum
    if ((type & 0xe0) === 0xe0) {
        if(debug) {
            logger.info('Negative fixed num');
        }
        //value = this.buffer.readInt8(this.offset);
        this.offset++;
        return type;
    }
    switch (type) {
        // raw 16
        case 0xda:
            if(debug) {
                logger.info('Raw 16');
            }
            length = this.buffer.readUInt16BE(this.offset + 1);
            this.offset += 3;
            return this.raw(length);
        // raw 32
        case 0xdb:
            if(debug) {
                logger.info('Raw 32');
            }
            length = this.buffer.readUInt32BE(this.offset + 1);
            this.offset += 5;
            return this.raw(length);
        // nil
        case 0xc0:
            if(debug) {
                logger.info('nil');
            }
            this.offset++;
            return null;
        // false
        case 0xc2:
            if(debug) {
                logger.info('False');
            }
            this.offset++;
            return false;
        // true
        case 0xc3:
            if(debug) {
                logger.info('True');
            }
            this.offset++;
            return true;
        // binary 8
        case 0xc4:
            if(debug) {
                logger.info('Binary 8');
            }
            length = this.buffer.readUInt8(this.offset + 1);
            this.offset += 2;
            return decode(this.buf(length), debug);
        // binary 16
        case 0xc5:
            if(debug) {
                logger.info('Binary 16');
            }
            length = this.buffer.readUInt16BE(this.offset + 1);
            this.offset += 3;
            return decode(this.buf(length), debug);
        // binary 32
        case 0xc6:
            if(debug) {
                logger.info('Binary 32');
            }
            length = this.buffer.readUInt32BE(this.offset + 1);
            this.offset += 5;
            return decode(this.buf(length), debug);
        // uint8
        case 0xcc:
            if(debug) {
                logger.info('Uint 8');
            }
            value = this.buffer[this.offset + 1];
            this.offset += 2;
            return value;
        // uint 16
        case 0xcd:
            if(debug) {
                logger.info('Uint 16');
            }
            value = this.buffer.readUInt16BE(this.offset + 1);
            this.offset += 3;
            return value;
        // uint 32
        case 0xce:
            if(debug) {
                logger.info('Uint 32');
            }
            value = this.buffer.readUInt32BE(this.offset + 1);
            this.offset += 5;
            return value;
        // uint64
        case 0xcf:
            if(debug) {
                logger.info('Uint 64');
            }
            value = this.buffer.readUInt32BE(this.offset + 1) << 8 + this.buffer.readUInt32BE(this.offset + 1 + 4);
            this.offset += 9;
            return value;
        // int 8
        case 0xd0:
            if(debug) {
                logger.info('Int 8');
            }
            value = this.buffer.readInt8(this.offset + 1);
            this.offset += 2;
            return value;
        // int 16
        case 0xd1:
            if(debug) {
                logger.info('Int 16');
            }
            value = this.buffer.readInt16BE(this.offset + 1);
            this.offset += 3;
            return value;
        // int 32
        case 0xd2:
            if(debug) {
                logger.info('Int 32');
            }
            value = this.buffer.readInt32BE(this.offset + 1);
            this.offset += 5;
            return value;
        // int 64
        case 0xd3:
            if(debug) {
                logger.info('Int 64');
            }
            value = this.buffer.readInt32BE(this.offset + 1) << 8 + this.buffer.readInt32BE(this.offset + 1 + 4);
            this.offset += 9;
            return value;
        // map 16
        case 0xde:
            if(debug) {
                logger.info('Map 16');
            }
            length = this.buffer.readUInt16BE(this.offset + 1);
            this.offset += 3;
            return this.map(length);
        // map 32
        case 0xdf:
            if(debug) {
                logger.info('Map 32');
            }
            length = this.buffer.readUInt32BE(this.offset + 1);
            this.offset += 5;
            return this.map(length);
        // array 16
        case 0xdc:
            if(debug) {
                logger.info('Array 16');
            }
            length = this.buffer.readUInt16BE(this.offset + 1);
            this.offset += 3;
            return this.array(length);
        // array 32
        case 0xdd:
            if(debug) {
                logger.info('Array 32');
            }
            length = this.buffer.readUInt32BE(this.offset + 1);
            this.offset += 5;
            return this.array(length);
        // String 8
        case 0xd9:
            if(debug) {
                logger.info('String 8');
            }
            length = this.buffer.readInt8(this.offset + 1);
            this.offset += 2;
            return this.buf(length);
        // String 16
        case 0xda:
            if(debug) {
                logger.info('String 16');
            }
            length = this.buffer.readInt16BE(this.offset + 1);
            this.offset += 3;
            return this.buf(length);
        // String 32
        case 0xdb:
            if(debug) {
                logger.info('String 32');
            }
            length = this.buffer.readUInt32BE(this.offset + 1);
            this.offset += 5;
            return this.buf(length);
        // float
        case 0xca:
            if(debug) {
                logger.info('Float');
            }
            value = this.buffer.readFloatBE(this.offset + 1);
            this.offset += 5;
            return value;
        // double
        case 0xcb:
            if(debug) {
                logger.info('Double');
            }
            value = this.buffer.readDoubleBE(this.offset + 1);
            this.offset += 9;
            return value;
    }
    throw new Error("Unknown type 0x" + type.toString(16));
};

function decode(buffer, debug) {
    if (debug) {
        logger.info("Bytes: " + buffer.toString('hex'));
        logger.info("Length: " + buffer.length);
    }
    var decoder = new Decoder(buffer);
    var ret = [];
    while (buffer !== undefined && decoder.offset < buffer.length) {
        ret.push(decoder.parse(debug));
        /* if (debug) {
            logger.info("Return value: " + ret);
        } */
    }
    /* if (debug) {
        logger.info("Decoded: " + ret);
    } */
    return ret;
};

function encodeableKeys (value) {
    return Object.keys(value).filter(function (e) {
        return typeof value[e] !== 'function' || value[e].toJSON;
    });
};

function encode(value, buffer, offset) {
    var type = typeof value;
    var length, size;

    // Strings Bytes
    if (type === "string") {

        length = Buffer.byteLength(value, 'utf8');
        // fix string
        if (length < 0x20) {
            buffer[offset] = length | 0xa0;
            buffer.write(value, offset + 1, length, 'utf8');
            return 1 + length;
        }
        // string 8
        if (length < 0x100) {
            buffer[offset] = 0xd9;
            buffer.writeUInt8(length, offset + 1);
            buffer.write(value, offset + 2, length, 'utf8');
            return 2 + length;
        }
        // string 16
        if (length < 0x10000) {
            buffer[offset] = 0xda;
            buffer.writeUInt16BE(length, offset + 1);
            buffer.write(value, offset + 3, length, 'utf8');
            return 3 + length;
        }
        // string 32
        if (length < 0x100000000) {
            buffer[offset] = 0xdb;
            buffer.writeUInt32BE(length, offset + 1);
            buffer.write(value, offset + 5, length, 'utf8');
            return 5 + length;
        }
    }

    if (Buffer.isBuffer(value)) {
        length = value.length;
        // buffer 8
        if (length < 0x100) {
            buffer[offset] = 0xc4;
            buffer.writeUInt8(length, offset + 1);
            value.copy(buffer, offset + 2, 0, length);
            return 2 + length;
        }
        // buffer 16
        if (length < 0x10000) {
            buffer[offset] = 0xc5;
            buffer.writeUInt16BE(length, offset + 1);
            value.copy(buffer, offset + 3, 0, length);
            return 3 + length;
        }
        // buffer 32
        if (length < 0x100000000) {
            buffer[offset] = 0xc6;
            buffer.writeUInt32BE(length, offset + 1);
            value.copy(buffer, offset + 5, 0, length);
            return 5 + length;
        }
    }

    if (type === "number") {
        // Floating Point
        if ((value << 0) !== value) {
            if (value < 3.4028234663852886E38) {
                // 32 bit float
                buffer[offset] = 0xca;
                buffer.writeFloatBE(value, offset + 1);
                return 5;
            } else {
                // 64 bit float
                buffer[offset] = 0xcb;
                buffer.writeDoubleBE(value, offset + 1);
                return 9;
            }
        }

        // Integers
        if (value >=0) {
            // positive fixnum
            if (value <= 0x80) {
                buffer.writeUInt8(value, offset);
                return 1;
            }
            // uint 8
            if (value < 0x100) {
                buffer[offset] = 0xcc;
                buffer.writeUInt8(value, offset + 1);
                return 2;
            }
            // uint 16
            if (value < 0x10000) {
                buffer[offset] = 0xcd;
                buffer.writeUInt16BE(value, offset + 1);
                return 3;
            }
            // uint 32
            if (value < 0x100000000) {
                buffer[offset] = 0xce;
                buffer.writeUInt32BE(value, offset + 1);
                return 5;
            }
            // uint 64
            if (value < 0x10000000000000000) {
                buffer[offset] = 0xcf;
                buffer.writeUInt32BE(value << 8, offset + 1);
                buffer.writeUInt32BE(value, offset + 4);
                return 9;
            }
            throw new Error("Number too big 0x" + value.toString(16));
        }
        // negative fixnum
        if (value >= -0x20) {
            buffer.writeInt8(value, offset);
            return 1;
        }
        // int 8
        if (value >= -0x80) {
            buffer[offset] = 0xd0;
            buffer.writeInt8(value, offset + 1);
            return 2;
        }
        // int 16
        if (value >= -0x8000) {
            buffer[offset] = 0xd1;
            buffer.writeInt16BE(value, offset + 1);
            return 3;
        }
        // int 32
        if (value >= -0x80000000) {
            buffer[offset] = 0xd2;
            buffer.writeInt32BE(value, offset + 1);
            return 5;
        }
        // int 64
        if (value >= -0x8000000000000000) {
            buffer[offset] = 0xd3;
            buffer.writeInt32BE(value << 8, offset + 1);
            buffer.writeInt32BE(value, offset + 4);
            return 9;
        }
        throw new Error("Number too small -0x" + value.toString(16).substr(1));
    }

    // undefined
    if (type === "undefined") {
        buffer[offset] = 0xc0;
        return 1;
    }

    // null
    if (value === null) {
        buffer[offset] = 0xc0;
        return 1;
    }

    // Boolean
    if (type === "boolean") {
        buffer[offset] = value ? 0xc3 : 0xc2;
        return 1;
    }

    // Custom toJSON function.
    if (typeof value.toJSON === 'function') {
        return encode(value.toJSON(), buffer, offset);
    }

    // Container Types
    if (type === "object") {

        size = 0;
        var isArray = Array.isArray(value);

        if (isArray) {
            length = value.length;
        } else {
            var keys = encodeableKeys(value);
            length = keys.length;
        }

        if (length < 0x10) {
            buffer[offset] = length | (isArray ? 0x90 : 0x80);
            size = 1;
        }
        else if (length < 0x10000) {
            buffer[offset] = isArray ? 0xdc : 0xde;
            buffer.writeUInt16BE(length, offset + 1);
            size = 3;
        }
        else if (length < 0x100000000) {
            buffer[offset] = isArray ? 0xdd : 0xdf;
            buffer.writeUInt32BE(length, offset + 1);
            size = 5;
        }

        if (isArray) {
            for (var i = 0; i < length; i++) {
                size += encode(value[i], buffer, offset + size);
            }
        } else {
            for (var i = 0; i < length; i++) {
                var key = keys[i];
                size += encode(key, buffer, offset + size);
                size += encode(value[key], buffer, offset + size);
            }
        }

        return size;
    }
    if (type === "function") return undefined;
    throw new Error("Unknown type " + type);
};

function sizeof(value) {
    var type = typeof value;
    var length, size;

    // Raw Bytes
    if (type === "string") {
        length = Buffer.byteLength(value, 'utf8');
        if (length < 0x20) {
            return 1 + length;
        }
        // string 8
        if (length < 0x100) {
            return 2 + length;
        }
        // string 16
        if (length < 0x10000) {
            return 3 + length;
        }
        // string 32
        if (length < 0x100000000) {
            return 5 + length;
        }
    }

    if (Buffer.isBuffer(value)) {
        length = value.length;
        // buffer 8
        if (length < 0x100) {
            return 2 + length;
        }
        // buffer 16
        if (length < 0x10000) {
            return 3 + length;
        }
        // buffer 32
        if (length < 0x100000000) {
            return 5 + length;
        }
    }

    if (type === "number") {
        // Floating Point
        // double
        if ((value << 0) !== value) {
            if (value < 3.4028234663852886E38) {
                // 32 bit float
                return 5;
            } else {
                // 64 bit float
                return 9;
            }
        }

        // Integers
        if (value >=0) {
            // positive fixnum
            if (value < 0x80) return 1;
            // uint 8
            if (value < 0x100) return 2;
            // uint 16
            if (value < 0x10000) return 3;
            // uint 32
            if (value < 0x100000000) return 5;
            // uint 64
            if (value < 0x10000000000000000) return 9;
            throw new Error("Number too big 0x" + value.toString(16));
        }
        // negative fixnum
        if (value >= -0x20) return 1;
        // int 8
        if (value >= -0x80) return 2;
        // int 16
        if (value >= -0x8000) return 3;
        // int 32
        if (value >= -0x80000000) return 5;
        // int 64
        if (value >= -0x8000000000000000) return 9;
        throw new Error("Number too small -0x" + value.toString(16).substr(1));
    }

    // Boolean, null, undefined
    if (type === "boolean" || type === "undefined" || value === null) return 1;

    if (typeof value.toJSON === 'function') {
        return sizeof(value.toJSON());
    }

    // Container Types
    if (type === "object") {
        if ('function' === typeof value.toJSON) {
            value = value.toJSON();
        }

        size = 0;
        if (Array.isArray(value)) {
            length = value.length;
            for (var i = 0; i < length; i++) {
                size += sizeof(value[i]);
            }
        }
        else {
            var keys = encodeableKeys(value);
            length = keys.length;
            for (var i = 0; i < length; i++) {
                var key = keys[i];
                size += sizeof(key) + sizeof(value[key]);
            }
        }
        if (length < 0x10) {
            return 1 + size;
        }
        if (length < 0x10000) {
            return 3 + size;
        }
        if (length < 0x100000000) {
            return 5 + size;
        }
        throw new Error("Array or object too long 0x" + length.toString(16));
    }
    if (type === "function") {
        return 0;
    }
    throw new Error("Unknown type " + type);
};