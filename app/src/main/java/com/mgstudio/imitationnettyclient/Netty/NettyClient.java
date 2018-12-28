package com.mgstudio.imitationnettyclient.Netty;

import android.util.Log;

import com.mgstudio.imitationnettyclient.Broadcast.NettyClientBroadcastUtil;
import com.mgstudio.imitationnettyclient.ConfigPara.Const;
import com.mgstudio.imitationnettyclient.Utils.DebugUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import timber.log.Timber;

public class NettyClient {

    private static final String tag = "NettyClient";

    private static NettyClient nettyClient = new NettyClient();

    private EventLoopGroup group;

    private NettyClientListener listener;

    private Channel channel;

    private boolean isConnected = false;

    private int reconnectNum = Integer.MAX_VALUE;

    private long reconnectIntervalTime = 5000;

    public static NettyClient getInstance() { return nettyClient; }

//    建立连接
    public synchronized NettyClient connect() {
        if(!isConnected) {
            group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channel(NioSocketChannel.class)
                    .handler(new NettyClientInitializer(listener));
            try {
                bootstrap.connect(Const.HOST, Const.TCP_PORT).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            isConnected = true;
                            channel = channelFuture.channel();//连接成功 则获取并保存channel
                        } else {
                            listener.onServiceStatusConnectChanged(NettyClientBroadcastUtil.STATUS_CONNECT_ERROR);
                            isConnected = false;
                        }
                    }
                }).sync();
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
                listener.onServiceStatusConnectChanged(NettyClientBroadcastUtil.STATUS_EXCEPTION);
                reconnect();
            }
        }
        return this;
    }

//    断开连接
    public void disconnect() {
        if(group!=null)group.shutdownGracefully();
    }

//    重连
    public void reconnect() {
        DebugUtils.debugLog_Timber(tag, "reconnect");
        if (reconnectNum > 0 && !isConnected) {
            reconnectNum--;
            try {
                Thread.sleep(reconnectIntervalTime);
            } catch (InterruptedException ignored) {
            }
            Timber.e("重新连接");
            disconnect();
            connect();
        } else {
            disconnect();
        }
    }


    /**
     * 异步发送
     *
     * @param data
     * @param listener
     * @return
     */
    public boolean sendMsgToServer(byte[] data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnected;  //判断channel是否存在 判断连接标志位
        if (flag) {
            ByteBuf buf = Unpooled.copiedBuffer(data);
            ChannelFuture channelFuture = channel.writeAndFlush(buf).addListener(listener);  //发送数据并添加监听器
        }
        return flag;
    }

    /**
     * 同步发送
     *
     * @param data
     * @return
     */
    public boolean sendMsgToServer(String data) {
        boolean flag = channel != null && isConnected;
        if (flag) {
            ChannelFuture channelFuture = channel.writeAndFlush(data + System.getProperty("line.separator")).awaitUninterruptibly();
            return channelFuture.isSuccess();
        }
        return false;
    }

    /**
     * 发送消息
     *
     * @param data
     * @param futureListener
     */
    public void sendMessage(byte[] data, FutureListener futureListener) {
        boolean flag = channel != null && isConnected;
        if (!flag) {
            Log.e(tag, "------尚未连接");
            return;
        }
        if (futureListener == null) {
            channel.writeAndFlush(data).addListener(new FutureListener() {
                @Override
                public void success() {
                    Log.e(tag, "发送成功--->");
                }

                @Override
                public void error() {
                    Log.e(tag, "发送失败--->");
                }
            });
        } else {
            channel.writeAndFlush(data).addListener(futureListener);
        }
    }

    /**
     * 设置重连次数
     *
     * @param reconnectNum
     */
    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    /**
     * 设置重连间隔
     *
     * @param reconnectIntervalTime
     */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    /**
     * 获取TCP连接状态
     *
     * @return
     */
    public boolean getConnectStatus() {
        return isConnected;
    }

    public void setConnectStatus(boolean status) {
        this.isConnected = status;
    }

    public void setListener(NettyClientListener listener) {
        this.listener = listener;
    }


}
