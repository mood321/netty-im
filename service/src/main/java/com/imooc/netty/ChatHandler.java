package com.imooc.netty;

import com.imooc.eunm.MsgActionEnum;
import com.imooc.netty.pojo.ChatMsg;
import com.imooc.netty.pojo.DataContent;
import com.imooc.netty.pojo.UserChannelRel;
import com.imooc.service.impl.UserServiceImp;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自己的处理消息
 * TextWebSocketFrame  为wesocket 处理文本的对象
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    /**
     * 用于管理所有 客户端的channel
     */
    public static ChannelGroup clients =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //客户端传输的消息
        String content = msg.text();
        log.info("接收的数据》》》》》{}" + content);
        Channel channel = ctx.channel();
        /* for (Channel channel : clients) {
            channel.writeAndFlush(new TextWebSocketFrame("服务器接收消息时间" +
                    LocalDate.now() + " 消息为" + content));
        }*/
        //和for 效果一致
       /* clients.writeAndFlush(new TextWebSocketFrame("服务器接收消息时间"+
               LocalDate.now()+" 消息为"+content));
*/
        //2  判断 消息的类型  去处理
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        if (action == MsgActionEnum.CONNECT.type) {
            //2.1  当websocket 第一连接 初始化channel 把userID 关联起来
            UserChannelRel.put(dataContent.getChatMsg().getSenderId(), channel);

            //测试
            for (Channel client : clients) {
                log.info(client.id().asLongText());
            }
            UserChannelRel.outprint();
        } else if (action == MsgActionEnum.CHAT.type) {
            //2.2 聊天记录的消息 添加数据库 标记消息签收状态
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgData = chatMsg.getMsg();
            String senderId = chatMsg.getSenderId();
            String receiverId = chatMsg.getReceiverId();
            UserServiceImp userServiceImp = (UserServiceImp) applicationContext.getBean("userServiceImp");
            String msgId = userServiceImp.saveMag(chatMsg);
            chatMsg.setMsgId(msgId);

            Channel aceptChannel = UserChannelRel.get(receiverId);
            if (aceptChannel == null) {
                // TODO  用户离线
            } else {
                Channel findChannel = clients.find(aceptChannel.id());
                if (findChannel == null) {
                    aceptChannel.writeAndFlush(new TextWebSocketFrame(
                            JsonUtils.objectToJson(chatMsg)));
                } else {
                    //TODO 用户离线
                }
            }


        } else if (action == MsgActionEnum.SIGNED.type) {
            //2.3 签收消息类型 修改状态
            UserServiceImp userServiceImp = (UserServiceImp) applicationContext.getBean("userServiceImp");
            //签收的消息id 字符
            String msgIdsStr = dataContent.getExtand();
            String[] msgIdss = msgIdsStr.split(",");
            List<String> msgIdsList = new ArrayList<>();
            Arrays.stream(msgIdss).forEach((mid) -> {
                if (StringUtils.isBlank(mid)) {
                    msgIdsList.add(mid);
                }
            });
            log.info(msgIdsList.toString());
            if (msgIdsList != null && msgIdsList.size() > 0) {
                userServiceImp.updateMagSigned(msgIdsList);
            }
        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            //2.4 心跳类型消息
            log.info("来自客户端的心跳包》》{}"+content);
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //当客户端连接之后 把连接channel 放在一起 同意管理
        clients.add(ctx.channel());

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //客户端 断开连接 自动从clients 删除

      //  System.out.println(ctx.channel().id().asShortText() + "短id 已断开连接");
        System.out.println(ctx.channel().id().asLongText() + "长id  已断开连接");
        clients.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        clients.remove(ctx.channel());
    }
}
