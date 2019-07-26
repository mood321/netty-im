package com.imooc.eunm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum  SearchFriendStatusEunm {
    SUCCESS(0,"OK"),
    USER_NOT_EXIST(1,"无此用户"),
    NOT_YOURSELF(2,"不能添加自己为好友"),
    ALREADY_FRIEND(3,"该用户已经是你的好友")
    ;
    public Integer  status;
    public String msg;

    public static String getMsgByKey(Integer status) {
        for (SearchFriendStatusEunm type : SearchFriendStatusEunm.values()) {
            if (type.getStatus()== status) {
                return type.msg;
            }
        }
        return null;
    }
}
