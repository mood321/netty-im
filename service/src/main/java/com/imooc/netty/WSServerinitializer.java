package com.imooc.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class WSServerinitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //http编解码
        channel.pipeline().addLast(new HttpServerCodec());
        //对写大数据流
        channel.pipeline().addLast(new ChunkedWriteHandler());
        //http 对http 数据封装  FullHttpResquest FullHttpResponse
        channel.pipeline().addLast(new HttpObjectAggregator(1024*64));
        /** * http  end*/

        //websocket 协议的支持  和路由 /ws
        // 包括了 处理tcp 握手动作 ping+pong 心跳检查
        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        channel.pipeline().addLast(new ChatHandler());
        /** 心跳管理  需要激活netty管理状态的handler*/
        /**
         * 心跳检测  单独 读写空闲 不做处理
         *            ALL 空闲 激活自定义handler  关闭channel
        */

        channel.pipeline().addLast(new IdleStateHandler(10,10,10, TimeUnit.SECONDS));
        channel.pipeline().addLast(new HreatBeatHandler());
    }
}
