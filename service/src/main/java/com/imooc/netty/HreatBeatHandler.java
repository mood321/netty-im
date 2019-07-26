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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 心跳处理
 *
 * @DES ChannelInboundHandlerAdapter 抽象类  不用实现read0 方法
 */
@Slf4j
public class HreatBeatHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //判断ect 是否是IdleStateEvent 用于判断 事件出发  读/写空闲/读写空闲
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evet = (IdleStateEvent) evt;
            if (evet.state() == IdleState.READER_IDLE) {
                log.info("进入读空闲");
            } else if (evet.state() == IdleState.WRITER_IDLE) {
                log.info("进入写空闲");
            } else if (evet.state() == IdleState.ALL_IDLE) {
                log.info("进入读写空闲,关闭通道 管理通道个数" + ChatHandler.clients.size());
                ctx.channel().close();
                log.info("通道关闭后 管理通道个数" + ChatHandler.clients.size());
            }
        }
    }
}
