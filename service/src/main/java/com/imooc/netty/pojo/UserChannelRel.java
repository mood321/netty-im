package com.imooc.netty.pojo;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @Des channel和用户id关联
 */
public class UserChannelRel {
    private static Map<String, Channel> manger = new HashMap<>();

    public static void put(String sendId, Channel channel) {
        manger.put(sendId, channel);
    }
    public static Channel get(String sendId) {
       return  manger.get(sendId);
    }
    public static   void outprint(){
        manger.entrySet().stream().forEach((entry)->{
            System.out.println("sendUserID :"+entry.getKey()
                +"channelId  :" +entry.getValue().id().asLongText());
    });
    }
}
