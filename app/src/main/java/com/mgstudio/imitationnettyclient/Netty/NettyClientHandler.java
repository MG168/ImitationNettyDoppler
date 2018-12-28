package com.mgstudio.imitationnettyclient.Netty;

import com.mgstudio.imitationnettyclient.Broadcast.NettyClientBroadcastUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private NettyClientListener listener;

    public NettyClientHandler(NettyClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyClient.getInstance().setConnectStatus(true);  // 设置连接状态
        listener.onServiceStatusConnectChanged(NettyClientBroadcastUtil.STATUS_CONNECT_SUCCESS);  // 连接成功回调
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyClient.getInstance().setConnectStatus(false);  // 设置连接状态
        listener.onServiceStatusConnectChanged(NettyClientBroadcastUtil.STATUS_CONNECT_CLOSED);  // 连接关闭回调
        NettyClient.getInstance().reconnect();  // 重连
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        listener.onMessageRespone(byteBuf);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {  // netty 心跳机制
        listener.onuserEventTriggered(ctx, evt);
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
//        NettyTcpClient.getInstance().setConnectStatus(false);
        listener.onServiceStatusConnectChanged(NettyClientBroadcastUtil.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        listener.onMessageComplete(ctx);
    }
}
