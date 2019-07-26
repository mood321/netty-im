package com.imooc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.stereotype.Component;

@Component
public class WSServer {


    private static class SignlationWSServer {
        static final WSServer intence = new WSServer();
    }

    public static WSServer getInstance() {
        return SignlationWSServer.intence;
    }


    private EventLoopGroup bootGroup;
    private EventLoopGroup workGroup;
    private ServerBootstrap server;
    private ChannelFuture channelFuture;
    private WSServer() {
        bootGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
       server = new ServerBootstrap();
        server.group(bootGroup, workGroup);
        server.childHandler(new WSServerinitializer());//处理器
        server.channel(NioServerSocketChannel.class);
    }

    public  void strat(Integer port){
        channelFuture = server.bind(port);
        System.out.println("netty websocker 启动成功....");
    }


}
