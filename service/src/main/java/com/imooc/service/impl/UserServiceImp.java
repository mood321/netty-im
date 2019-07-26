package com.imooc.service.impl;

import com.imooc.eunm.MsgActionEnum;
import com.imooc.eunm.MsgSignFlagEnum;
import com.imooc.eunm.SearchFriendStatusEunm;
import com.imooc.mapper.*;
import com.imooc.netty.pojo.ChatMsg;
import com.imooc.netty.pojo.DataContent;
import com.imooc.netty.pojo.UserChannelRel;
import com.imooc.pojo.FriendsRequest;
import com.imooc.pojo.MyFriends;
import com.imooc.pojo.Users;
import com.imooc.pojo.UsersExample;
import com.imooc.pojo.bo.UsersBo;
import com.imooc.pojo.vo.FriendRequestVo;
import com.imooc.service.UserService;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.IdWorker;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    UsersMapper usersMapper;
    @Autowired
    MyFriendsMapper myFriendsMapper;
    @Autowired
    FriendsRequestMapper friendsRequestMapper;
    @Autowired
    UsersMapperCustom usersMapperCustom;
    @Autowired
    ChatMsgMapper chatMsgMapper;

    @Autowired
    Sid sid;
    @Autowired
    FastDFSClient fastDFSClient;
    @Autowired
    QRCodeUtils qrCodeUtils;

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @Override
    public boolean queryUserNameIsExist(String userName) {
        Users users = new Users();
        users.setUsername(userName);
        Users userByName = usersMapper.selectOne(users);
        return userByName == null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @Override
    public Users queryForLogin(String username, String password) {
        Example usersExample = new Example(Users.class);
        Example.Criteria criteria = usersExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);
        Users users = usersMapper.selectOneByExample(usersExample);
        return users;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Users saveUser(Users user) {
        String id = sid.nextShort();
        //TODO 二维码
        /**
         * path：tem/user/userid.png
         * 生成二维码 QR:[username]
         */
        String rootPath = System.getProperty("user.dir");

        String qrPath = rootPath + File.separator + "tem" + File.separator + "user" + File.separator + id + ".png";
        qrCodeUtils.createQRCode(qrPath, "mim_qr:" + user.getUsername());
        MultipartFile qrImg = FileUtils.fileToMultipart(qrPath);
        String qrFafsPath = null;
        try {
            qrFafsPath = fastDFSClient.uploadQRCode(qrImg);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("二维码上传失败");
        }
        System.out.println(qrFafsPath);
        user.setQrcode(qrFafsPath);
        user.setId(id);
        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Users updateUserInfo(Users users) {
        int i = usersMapper.updateByPrimaryKeySelective(users);
        return i > 0 ? queryUserById(users.getId()) : null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserById(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users setNickNmae(UsersBo userBo) {
        Users users = new Users();
        users.setId(userBo.getUserId());
        users.setNickname(userBo.getNickname());
        int i = usersMapper.updateByPrimaryKeySelective(users);
        return i > 0 ? queryUserById(users.getId()) : null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer searchUserByName(String myUserId, String friendUserName) {
        Users friend = queryUserByName(friendUserName);
        if (friend == null) {
            return SearchFriendStatusEunm.USER_NOT_EXIST.status;
        }
        if (myUserId.equals(friend.getId())) {
            return SearchFriendStatusEunm.NOT_YOURSELF.status;
        }
        /** */
        Example mfriendEx = new Example(MyFriends.class);
        Example.Criteria friCriteria = mfriendEx.createCriteria();
        friCriteria.andEqualTo("myUserId", myUserId);
        friCriteria.andEqualTo("myFriendUserId", friend.getId());
        MyFriends myFriends = myFriendsMapper.selectOneByExample(mfriendEx);
        if (myFriends != null) {
            return SearchFriendStatusEunm.ALREADY_FRIEND.status;
        }

        return SearchFriendStatusEunm.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserByName(String userName) {
        Example ue = new Example(Users.class);
        Example.Criteria criteria = ue.createCriteria();
        criteria.andEqualTo("username", userName);
        return usersMapper.selectOneByExample(ue);
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void sendFriend(String myUserId, String friendUserName) {
        Users friend = queryUserByName(friendUserName);

        Example friendsRequestExmple = new Example(FriendsRequest.class);
        Example.Criteria criteria = friendsRequestExmple.createCriteria();
        criteria.andEqualTo("sendUserId", myUserId);
        criteria.andEqualTo("acceptUserId", friend.getId());

        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(friendsRequestExmple);
        if (friendsRequest == null) {
            FriendsRequest myFriends = new FriendsRequest();
            myFriends.setId(sid.nextShort());
            myFriends.setSendUserId(myUserId);
            myFriends.setAcceptUserId(friend.getId());
            myFriends.setRequestDateTime(new Date());
            friendsRequestMapper.insert(myFriends);
        }

    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<FriendRequestVo> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example friendExample = new Example(FriendsRequest.class);
        Example.Criteria friendCriteria = friendExample.createCriteria();
        friendCriteria.andEqualTo("sendUserId", sendUserId);
        friendCriteria.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(friendExample);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriend(sendUserId, acceptUserId);
        saveFriend(acceptUserId, sendUserId);
        deleteFriendRequest(sendUserId, acceptUserId);
        //使用websocket 主动推动消息  更新好友列表
        Channel userChannel = UserChannelRel.get(sendUserId);
        if (userChannel != null) {
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            userChannel.writeAndFlush(new TextWebSocketFrame(
                    JsonUtils.objectToJson(dataContent) ));
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFriend(String sendUserId, String acceptUserId) {
        MyFriends myFriends = new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriendsMapper.insert(myFriends);
    }

    @Override
    public List queryMyfriends(String userId) {
        return usersMapperCustom.queryMyfriends(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String saveMag(ChatMsg chatMsg) {
        com.imooc.pojo.ChatMsg msgDB = new com.imooc.pojo.ChatMsg();
        String id = sid.nextShort();
        msgDB.setId(id);
        msgDB.setCreateTime(new Date());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setMsg(chatMsg.getMsg());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);

        chatMsgMapper.insert(msgDB);
        return id;
    }

    /**
     * 批量处理 签收消息
     *
     * @param msgIdsList
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateMagSigned(List<String> msgIdsList) {
        usersMapperCustom.batchupdateMagSigned(msgIdsList);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<com.imooc.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        Example chatExample = new Example(com.imooc.pojo.ChatMsg.class);
        Example.Criteria criteria = chatExample.createCriteria();
        criteria.andEqualTo("acceptUserId", acceptUserId);
        criteria.andEqualTo("signFlag", 1);
        return chatMsgMapper.selectByExample(chatExample);
    }
}
