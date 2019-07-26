package com.imooc.netty.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataContent implements Serializable {

    private Integer action;//消息类型
    private ChatMsg chatMsg;//消息主体

    private String extand;

}
