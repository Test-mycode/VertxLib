package com.pqitech.message

import io.vertx.core.buffer.Buffer

data class MessageHeader(var id : Long = 0, var type : Int = 0,var length : Long = 0, var compress : Byte = 0x00, var crypt : Byte = 0x00.toByte()) {

    fun toBuffer() : Buffer
    {
        val buffer = Buffer.buffer()
        writeToBuffer(buffer)
        return buffer
    }

    fun writeToBuffer(buffer : Buffer)
    {
        buffer.appendByte('P'.toByte())
        buffer.appendByte('Q'.toByte())
        buffer.appendUnsignedInt(id)
        buffer.appendUnsignedShort(type)
        buffer.appendUnsignedInt(length)
        buffer.appendByte(compress)
        buffer.appendByte(crypt)
        buffer.appendByte('p'.toByte())
        buffer.appendByte('q'.toByte())
    }

    fun returnHeader() : MessageHeader
    {
        val header = MessageHeader()
        header.id = this.id
        header.type = this.type + 1;
        header.length = 0;
        header.compress = this.compress
        header.crypt = this.crypt
        return header
    }
}
