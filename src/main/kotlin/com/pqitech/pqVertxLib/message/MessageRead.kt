package com.pqitech.pqVertxLib.message

import io.vertx.core.buffer.Buffer

class MessageRead(val hander : (MessageHeader, Buffer?)->Unit, val erroHandle : (()->Boolean)? = null)
{
    private var _bytePos : Int = 0
    private var _needBytes : Int = 0
    private var _status = 0
    private val byteBuffer : Buffer = Buffer.buffer(4)
    private var body : Buffer? = null
    private var header : MessageHeader = MessageHeader()

    fun readMessage(buffer : Buffer) : Boolean{
        val max = buffer.length()
        var pos = 0
        while(pos < max){
            val byte = buffer.getByte(pos)
            when(_status){
                0 -> {
                  // checkPG
                    if(byte == 'P'.toByte()){
                        _status ++
                    } else {
                        val toContinue = erroHandle?.invoke() ?: true
                        if(!toContinue) return false
                    }
                    pos ++
                }
                1 -> { // Q
                    if(byte == 'Q'.toByte()){
                        _status ++
                        _bytePos = 0
                        pos ++
                    } else {
                        _status = 0
                        val toContinue = erroHandle?.invoke() ?: true
                        if(!toContinue) return false
                    }

                }
                2 -> { // ID
                    if(readHeaderColumn(byte,4)){
                        header.id = byteBuffer.getUnsignedInt(0)
                        _bytePos = 0
                        _status ++
                    }
                    pos ++
                }
                3 -> { // type
                    if(readHeaderColumn(byte,2)){
                        header.type = byteBuffer.getUnsignedShort(0)
                        _bytePos = 0
                        _status ++
                    }
                    pos ++
                }
                4 -> {
                  // length
                    if(readHeaderColumn(byte,4)){
                        header.length = byteBuffer.getUnsignedInt(0)
                        _bytePos = 0
                        _status ++
                    }
                    pos ++
                }
                5 -> {
                    header.compress = byte
                    _status ++
                    pos ++
                }
                6 -> {
                    header.crypt = byte
                    _status ++
                    pos ++
                }
                7 -> { // CRC1
                    if(byte == 'p'.toByte()){
                        _status ++
                        pos ++
                    } else {
                        _status = 0
                        val toContinue = erroHandle?.invoke() ?: true
                        if(!toContinue) return false
                    }

                }
                8 -> { // CRC2
                    if(byte == 'q'.toByte()){
                        pos ++
                        _bytePos = 0
                        _needBytes = header.length.toInt()
                        if(header.length > 0 ){
                            _status ++
                            body = Buffer.buffer()
                        } else {
                            _status = 0
                            hander.invoke(header,null)
                            header = MessageHeader()
                        }
                    } else {
                        _status = 0
                        val toContinue = erroHandle?.invoke() ?: true
                        if(!toContinue) return false
                    }
                }
                9 -> {
                    val (next_, read_) = readData(buffer,pos,max)
                    pos += read_
                    if(next_){
                        _status = 0
                        hander.invoke(header,body)
                        header = MessageHeader()
                        body = null
                    }
                }
                else ->{ // read data
                    _status = 0
                }
            }
        }
        return true
    }

    private  fun readData(buffer: Buffer,pos : Int, max : Int) : Pair<Boolean,Int>
    {
        val canRead = max - pos
        val retBool : Boolean
        val retRead : Int
        if(canRead >= _needBytes){
            body?.appendBuffer(buffer.slice(pos,(pos + _needBytes)))
            retRead = _needBytes
            _needBytes = 0
            retBool = true
        } else {
            body?.appendBuffer(buffer.slice(pos,(pos + canRead)))
            retRead = canRead
            _needBytes -= canRead
            retBool = false
        }
        return Pair<Boolean,Int>(retBool,retRead)
    }

    private fun readHeaderColumn(byte : Byte, max : Int) : Boolean{
        byteBuffer.setByte(_bytePos,byte)
        _bytePos ++
        return (_bytePos >= max)
    }
}
