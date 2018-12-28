package com.mgstudio.imitationnettyclient.Netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private NettyClientListener listener;  // 用于回调

    private int WRITE_WAIT_SECONDS = 10;

    private  int READ_WAIT_SECONDS = 13;

    public NettyClientInitializer(NettyClientListener listener) {
        this.listener = listener;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();//获取
//        SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//        pipeline.addLast(sslCtx.newHandler(ch.alloc()));//SSL handler netty默认实现
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));    // 开启日志，可以设置日志等级
        ch.pipeline().addLast("ping", //netty 心跳机制
                new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));//5s未发送数据，回调userEventTriggered
//        pipeline.addLast("StringDecoder", new StringDecoder());//String解码器   用于SimpleChannelInboundHandler<String>
//        pipeline.addLast("StringEncoder", new StringEncoder());//String编码器   用于SimpleChannelInboundHandler<String>
        pipeline.addLast(new NettyClientHandler(listener));

    }
}
