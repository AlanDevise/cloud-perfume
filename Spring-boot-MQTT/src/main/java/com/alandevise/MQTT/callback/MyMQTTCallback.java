package com.alandevise.MQTT.callback;

import com.alandevise.MQTT.client.MyMQTTClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyMQTTCallback implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyMQTTCallback.class);

    private MyMQTTClient myMQTTClient;

    public MyMQTTCallback(MyMQTTClient myMQTTClient) {
        this.myMQTTClient = myMQTTClient;
    }

    /**
     * 丢失连接，可在这里做重连
     * 只会调用一次
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        LOGGER.error("mqtt connectionLost 连接断开，5S之后尝试重连: {}", throwable.getMessage());
        long reconnectTimes = 1;
        while (true) {
            try {
                if (MyMQTTClient.getClient().isConnected()) {
                    LOGGER.warn("mqtt reconnect success end");
                    return;
                }
                LOGGER.warn("mqtt reconnect times = {} try again...", reconnectTimes++);
                MyMQTTClient.getClient().reconnect();
            } catch (MqttException e) {
                LOGGER.error("", e);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
            }
        }
    }

    /**
     * @param topic
     * @param mqttMessage
     * @throws Exception
     * subscribe后得到的消息会执行到这里面
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        LOGGER.info("接收消息主题 : {}，接收消息内容 : {}", topic, new String(mqttMessage.getPayload()));
//        if (topic.contains("A/pick/warn")) {
//            //业务接口
//            insertPushAlarm(new String(message.getPayload(), CharsetUtil.UTF_8));
//        } else if (topic.equals("A/cmd/resp")) {
//            Map maps = (Map) JSON.parse(new String(message.getPayload(), CharsetUtil.UTF_8));
//            //业务接口
//            insertCmdResults(maps);
//        }
    }

    /**
     * 消息到达后
     * subscribe后，执行的回调函数
     *
     * @param s
     * @param mqttMessage
     * @throws Exception
     */
    /**
     * publish后，配送完成后回调的方法
     *
     * @param iMqttDeliveryToken
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        LOGGER.info("==========deliveryComplete={}==========", iMqttDeliveryToken.isComplete());
    }
}

