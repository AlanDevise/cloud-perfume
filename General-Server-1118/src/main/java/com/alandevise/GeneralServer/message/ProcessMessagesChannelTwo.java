// package com.alandevise.message;
//
// import org.springframework.stereotype.Component;
//
// /**
//  * @Filename: ProcessMessagesChannelTwo.java
//  * @Package: com.alandevise.message
//  * @Version: V1.0.0
//  * @Description: 1.
//  * @Author: Alan Zhang [initiator@alandevise.com]
//  * @Date: 2023年06月22日 13:52
//  */
//
// @Component
// public class ProcessMessagesChannelTwo {
//
//     public synchronized void defaultProcessMethod(String message) {
//         try {
//             System.out.println("我监听到频道2的消息啦,消息是:" + message);
//         } catch (Exception e) {
//             System.out.println("消息监听失败啦~~~~~~~");
//         }
//     }
// }