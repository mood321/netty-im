package com.imooc.service;

import com.imooc.netty.pojo.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UsersBo;
import com.imooc.pojo.vo.FriendRequestVo;

import java.util.List;

public interface UserService {
    /**
     *  判断用户名是否存在
     * @param userName
     * @return
     */
    public boolean queryUserNameIsExist(String userName);

    Users queryForLogin(String username, String password);

    Users saveUser(Users user);

    Users updateUserInfo(Users users);
    Users setNickNmae(UsersBo userBo);

    /**
     * 搜索朋友
     * @param myUserId
     * @param friendUserName
     * @return
     */
    Integer searchUserByName(String  myUserId,String friendUserName);

    /**
     * 通过 用户名搜索用户
     * @param userName
     * @return
     */
    public Users queryUserByName(String userName);

    void sendFriend(String myUserId, String friendUserName);

    List<FriendRequestVo> queryFriendRequestList(String  acceptUserId);

    /**
     * 忽略好友请求
     * @param sendUserId
     * @param acceptUserId
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 通过好友请求
     * @param sendUserId
     * @param acceptUserId
     */
    void passFriendRequest(String sendUserId, String acceptUserId);

    /**
     *  好友列表
     * @param userId
     * @return
     */
    List queryMyfriends(String userId);

    /**
     * 保存聊天记录
     * @param chatMsg
     * @return
     */
    String  saveMag(ChatMsg chatMsg);

    void updateMagSigned(List<String> msgIdsList);

    /**
     * 获取为签收的 消息列表
     * @param acceptUserId
     * @return
     */
    List getUnReadMsgList(String acceptUserId);
}
