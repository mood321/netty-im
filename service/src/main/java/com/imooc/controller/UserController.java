package com.imooc.controller;

import com.imooc.eunm.OperFriendStatusEunm;
import com.imooc.eunm.SearchFriendStatusEunm;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UsersBo;
import com.imooc.pojo.vo.UsersVo;
import com.imooc.service.impl.UserServiceImp;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/u")
@Slf4j
public class UserController {
    @Autowired
    UserServiceImp userServiceImp;
    @Autowired
    FastDFSClient fastDFSClient;
    @Value("${fdfs.thumbImage.width}")
    String width;
    @Value("${fdfs.thumbImage.height}")
    String height;

    @PostMapping("/registOrLogin")
    public IMoocJSONResult registOrLogin(@RequestBody Users user) throws Exception {
        System.out.println("log");
        if (StringUtils.isEmpty(user.getUsername()) ||
                StringUtils.isEmpty(user.getPassword())) {
            return IMoocJSONResult.errorMsg("用户名或密码为空");
        }
        boolean existsFlag = userServiceImp.queryUserNameIsExist(user.getUsername());
        Users users = null;
        if (!existsFlag) {
            users = userServiceImp.queryForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
            if (users == null) {
                return IMoocJSONResult.errorMsg("用户名或密码不正确");
            }
        } else {
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            users = userServiceImp.saveUser(user);
        }
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(users, usersVo);
        return IMoocJSONResult.ok(usersVo);
    }

    /**
     * 上传一个basse64的图片
     *
     * @return
     */
    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBo usersBo) throws Exception {
        //获取内容
        String baseData = usersBo.getFaceData();
        String rootPath = System.getProperty("user.dir");
        String partPath = rootPath + File.separator + "tem";
        if (!new File(partPath).exists())
            new File(partPath).mkdirs();
        String imgPath = partPath + File.separator + usersBo.getUserId() + ".png";
        com.imooc.utils.FileUtils.base64ToFile(imgPath, baseData);
        MultipartFile img = FileUtils.fileToMultipart(imgPath);
        String faceFafsPath = fastDFSClient.uploadBase64(img);
        log.info(faceFafsPath);
        // "asdasdasdasdasdasdasdas.png"
        // "asdasdasdasdasdasdasdas_80x80.png"
        //缩略图
        String thump = width + "x" + height;
        log.info(thump);
        String[] arr = faceFafsPath.split("\\.");
        String thumpFacePath = arr[0] + "_" + thump + "." + arr[1];
        //用户更新
        Users users = new Users();
        users.setId(usersBo.getUserId());
        users.setFaceImageBig(faceFafsPath);
        users.setFaceImage(thumpFacePath);
        Users result = userServiceImp.updateUserInfo(users);
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(result, usersVo);
        return result != null ? IMoocJSONResult.ok(usersVo) : IMoocJSONResult.errorMsg("上传失败!");
    }

    /**
     * 更新昵称
     *
     * @return
     */
    @PostMapping("/setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBo usersBo) throws Exception {
        Users result = userServiceImp.setNickNmae(usersBo);
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(result, usersVo);
        return result != null ? IMoocJSONResult.ok(usersVo) : IMoocJSONResult.errorMsg("上传失败!");
    }

    /**
     * 更新昵称
     *
     * @return
     */
    @PostMapping("/search")
    public IMoocJSONResult search(String myUserId, String friendUserName) throws Exception {
        log.info("/u/search");
        if (StringUtils.isEmpty(myUserId) || StringUtils.isEmpty(friendUserName)) {
            return IMoocJSONResult.errorMsg("好友名称不能空");
        }
        //前置条件  - 不存在 用户
        //前置条件  - 搜索的是自己
        //前置条件  - 搜索的 已经是你的好友
        Integer status =
                userServiceImp.searchUserByName(myUserId, friendUserName);
        if (status == SearchFriendStatusEunm.SUCCESS.status) {

            Users users = userServiceImp.queryUserByName(friendUserName);
            UsersVo usersVo = new UsersVo();
            BeanUtils.copyProperties(users, usersVo);
            return IMoocJSONResult.ok(usersVo);
        } else {
            String errorMsg = SearchFriendStatusEunm.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }

    }

    /**
     * 添加好友请求
     *
     * @return
     */
    @PostMapping("/addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUsername) {
        log.info("/u/addFriendRequest");
        if (StringUtils.isEmpty(myUserId) || StringUtils.isEmpty(friendUsername)) {
            return IMoocJSONResult.errorMsg("好友名称不能空");
        }
        //前置条件  - 不存在 用户
        //前置条件  - 搜索的是自己
        //前置条件  - 搜索的 已经是你的好友
        Integer status =
                userServiceImp.searchUserByName(myUserId, friendUsername);
        if (status == SearchFriendStatusEunm.SUCCESS.status) {
            userServiceImp.sendFriend(myUserId, friendUsername);
        } else {
            String errorMsg = SearchFriendStatusEunm.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
        return IMoocJSONResult.ok("申请成功");
    }

    /**
     * 添加好友请求
     *
     * @return
     */
    @PostMapping("/queryFriendRequestList")
    public IMoocJSONResult queryFriendRequestList(String userId) {
        log.info("/u/queryFriendRequestList");
        if (StringUtils.isEmpty(userId)) {
            return IMoocJSONResult.errorMsg("编号不能为空");
        }

        return IMoocJSONResult.ok(userServiceImp.queryFriendRequestList(userId));
    }

    /**
     * 添加好友请求
     *
     * @return
     */
    @PostMapping("/operFriendRequest")
    public IMoocJSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType) {
        log.info("/u/queryFriendRequestList");
        if (StringUtils.isEmpty(acceptUserId) || StringUtils.isEmpty(sendUserId)
                || operType == null) {
            return IMoocJSONResult.errorMsg("参数不能为空");
        }
        //没有对应的 类型  直接失败
        if (StringUtils.isBlank(OperFriendStatusEunm.getMsgByKey(operType))) {
            return IMoocJSONResult.errorMsg("没有这个类型");
        }
        if (OperFriendStatusEunm.FAIL.status == operType) {
            userServiceImp.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (OperFriendStatusEunm.PASS.status == operType) {

            //通过 则添加好友  并删除好友请求
            userServiceImp.passFriendRequest(sendUserId, acceptUserId);
        }
        /**
         * 通过之后 刷新好友列表
         */
        List list = userServiceImp.queryMyfriends(acceptUserId);
        return IMoocJSONResult.ok(list);
    }

    /**
     * 好友列表
     *
     * @return
     */
    @PostMapping("/myFriends")
    public IMoocJSONResult myFriends(String userId) {
        log.info("/u/myFriends");
        if (StringUtils.isEmpty(userId)) {
            return IMoocJSONResult.errorMsg("参数不能为空");
        }
        List list = userServiceImp.queryMyfriends(userId);
        return IMoocJSONResult.ok(list);
    }

    /**
     * 用户获取 未签收消息
     * @param acceptUserId
     * @return
     */
    @PostMapping("/getUnReadMsgList")
    public IMoocJSONResult getUnReadMsgList(String acceptUserId) {
        log.info("/u/getUnReadMsgList");
        if (StringUtils.isEmpty(acceptUserId)) {
            return IMoocJSONResult.errorMsg("参数不能为空");
        }
        //查询列表
        List list = userServiceImp.getUnReadMsgList(acceptUserId);
        return IMoocJSONResult.ok(list);
    }


}
