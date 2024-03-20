package com.alandevise.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Filename: WebSocket.java
 * @Package: com.alandevise.component
 * @Version: V1.0.0
 * @Description: 1. websocket的组件逻辑实现类
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2024年03月18日 22:04
 */


@ServerEndpoint("/websocket/{pageCode}")
//  @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,注解的值将被用于监听用户连接的终端访问URL地址,
//  客户端可以通过这个URL来连接到WebSocket服务器端
@Component
@Slf4j
public class WebSocket {

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
     */
    public static Map<String, List<Session>> userSocketMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam("pageCode") String pageCode, Session session) {
        List<Session> sessions = userSocketMap.get(pageCode);
        if (null == sessions) {
            List<Session> sessionList = new ArrayList<>();
            sessionList.add(session);
            userSocketMap.put(pageCode, sessionList);
        } else {
            sessions.add(session);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("pageCode") String pageCode, Session session) {
        if (userSocketMap.containsKey(pageCode)) {
            userSocketMap.get(pageCode).remove(session);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("websocket received message:" + message);
        try {
            for (List<Session> sessionList : userSocketMap.values()) {
                for (Session value : sessionList) {
                    value.getBasicRemote().sendText("这是推送测试数据！您刚发送的消息是：" + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
    }
}