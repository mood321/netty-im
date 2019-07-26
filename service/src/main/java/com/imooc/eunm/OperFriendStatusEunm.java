package com.imooc.eunm;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum OperFriendStatusEunm {
    FAIL  (0,"忽略"),
    PASS (1,"通过好友请求"),

    ;
    public Integer  status;
    public String msg;

    public static String getMsgByKey(Integer status) {
        for (OperFriendStatusEunm type : OperFriendStatusEunm.values()) {
            if (type.getStatus()== status) {
                return type.msg;
            }
        }
        return null;
    }
}
