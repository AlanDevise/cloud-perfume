package com.alandevise.nettyTool;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteOutput;
import org.jboss.marshalling.Marshaller;

import java.io.IOException;

/**
 * @Filename: MarshallingEncoder.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:59
 */

public class MarshallingEncoder {

    //空白占位: 用于预留设置 body的数据包长度
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
    Marshaller marshaller;

    public MarshallingEncoder() throws IOException {
        marshaller = MarshallingCodecFactory.buildMarshalling();
    }

    protected void encode(Object msg, ByteBuf out) throws Exception {
        try {
            //必须要知道当前的数据位置是哪: 起始数据位置
            //长度属性的位置索引
            int lengthPos = out.writerIndex();
            //占位写操作:先写一个4个字节的空的内容，记录在起始数据位置，用于设置内容长度
            out.writeBytes(LENGTH_PLACEHOLDER);
            ChannelBufferByteOutput output = new ChannelBufferByteOutput(out);
            marshaller.start((ByteOutput) output);
            marshaller.writeObject(msg);
            marshaller.finish();
            //总长度(结束位置) - 初始化长度(起始位置) - 预留的长度  = body数据长度
            int endPos = out.writerIndex();
            out.setInt(lengthPos, endPos - lengthPos - 4);
        } finally {
            marshaller.close();
        }
    }

}