package com.alandevise.nettyTool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;

/**
 * @Filename: ChannelBufferByteInput.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1.消息编码器
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:44
 */



public class ChannelBufferByteInput extends ByteBufInputStream {

    private final ByteBuf buffer;

    public ChannelBufferByteInput(ByteBuf buffer) {
        super(buffer);
        this.buffer = buffer;
    }


    @Override
    public void close() throws IOException {
        // nothing to do
    }

    @Override
    public int available() throws IOException {
        return buffer.readableBytes();
    }

    @Override
    public int read() throws IOException {
        if (buffer.isReadable()) {
            return buffer.readByte() & 0xff;
        }
        return -1;
    }

    @Override
    public int read(byte[] array) throws IOException {
        return read(array, 0, array.length);
    }

    @Override
    public int read(byte[] dst, int dstIndex, int length) throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }

        length = Math.min(available, length);
        buffer.readBytes(dst, dstIndex, length);
        return length;
    }

    @Override
    public long skip(long bytes) throws IOException {
        int readable = buffer.readableBytes();
        if (readable < bytes) {
            bytes = readable;
        }
        buffer.readerIndex((int) (buffer.readerIndex() + bytes));
        return bytes;
    }
}

