package com.alandevise.nettyTool;

import org.jboss.marshalling.*;

import java.io.IOException;

/**
 * @Filename: MarshallingCodecFactory.java
 * @Package: com.alandevise.nettyTool
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年04月01日 14:57
 */

public class MarshallingCodecFactory {

    /**
     * 创建Jboss Marshaller
     *
     * @return
     * @throws IOException
     */
    protected static Marshaller buildMarshalling() throws IOException {
        final MarshallerFactory marshallerFactory = Marshalling
                .getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        Marshaller marshaller = marshallerFactory
                .createMarshaller(configuration);
        return marshaller;
    }

    /**
     * 创建Jboss Unmarshaller
     *
     * @return
     * @throws IOException
     */
    protected static Unmarshaller buildUnMarshalling() throws IOException {
        final MarshallerFactory marshallerFactory = Marshalling
                .getProvidedMarshallerFactory("serial");

        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        final Unmarshaller unmarshaller = marshallerFactory
                .createUnmarshaller(configuration);
        return unmarshaller;
    }
}
