package com.imooc.mapper;

import java.util.List;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVo;
import com.imooc.pojo.vo.MyFriendsVo;
import com.imooc.utils.MyMapper;

public interface UsersMapperCustom extends MyMapper<Users> {


    List<FriendRequestVo> queryFriendRequestList(String  acceptUserId);

    List<MyFriendsVo>  queryMyfriends(String userId);

    int batchupdateMagSigned(List<String> msgIdsList);
}