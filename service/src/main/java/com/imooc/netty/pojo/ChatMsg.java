package com.imooc.netty.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatMsg implements Serializable {

    private String msg;//消息
    private String senderId;//发送者ID
    private String receiverId;//接受者id
    private String msgId;//同于消息 签收
}
