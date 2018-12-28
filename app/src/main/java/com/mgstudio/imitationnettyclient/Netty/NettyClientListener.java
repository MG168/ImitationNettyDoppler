package com.mgstudio.imitationnettyclient.Netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface NettyClientListener {

    /**
     *  当接收系统消息
     */
    void  onMessageRespone(ByteBuf byteBuf);

    /**
     * 当通信完成
     */
    void onMessageComplete(ChannelHandlerContext cxt);

    /**
     * 心跳
     */
    void onuserEventTriggered(ChannelHandlerContext ctx, Object evt);

    /**
     * 当服务状态发生变化时触发
     */
    public void onServiceStatusConnectChanged(int statusCode);

}
