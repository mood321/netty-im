package com.imooc;

import com.imooc.netty.WSServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import sun.applet.AppletEvent;
import sun.applet.AppletListener;

@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent evet) {
        if(evet.getApplicationContext().getParent()==null){
            try {
                WSServer.getInstance().strat(8088);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
