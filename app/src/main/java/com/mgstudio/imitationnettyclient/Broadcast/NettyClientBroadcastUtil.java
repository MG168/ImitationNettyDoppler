package com.mgstudio.imitationnettyclient.Broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class NettyClientBroadcastUtil {
    /**
     * Status constants
     */
    public final static String ACTION_NETTY_SOCKET_DATA_RECEIVE =
            "com.LIN.NETTY.Socket.ACTION_NETTY_SOCKET_DATA_RECEIVE";  // SOCKET数据接收
    public final static String ACTION_NETTY_SOCKET_ERROR =
            "com.LIN.NETTY.Socket.ACTION_NETTY_SOCKET_ERROR";  // SOCKET错误
    public final static String ACTION_NETTY_SOCKET_DATA_SEND =
            "com.LIN.NETTY.Socket.ACTION_NETTY_SOCKET_DATA_SEND";  // SOCKET数据发送
    public static final String ACTION_NETTY_SOCKET_STATE_CHANGED =
            "com.LIN.NETTY.Socket.ACTION.STATE_CHANGED";  // SOCKET状态变化

    public static final int STATUS_CONNECT_SUCCESS = 0;

    public static final int  STATUS_CONNECT_CLOSED = 1;

    public static final int  STATUS_CONNECT_ERROR = 2;

    public static final int  STATUS_CONNECTING = 3;

    public static final int  STATUS_CONNECT_START = 4;

    public static final int  STATUS_CONNECT_RESTART = 5;

    public static final int  STATUS_EXCEPTION_INTERNET = 6;

    public static final int  STATUS_EXCEPTION = 7;

    public static final int  STATUS_SEND_SUCCESS = 10;

    public static final int  STATUS_SEND_ERROR = 11;

    public static final int  STATUS_RECEIVE_SUCCESS = 12;

    public static final int  STATUS_RECEIVE_ERROR = 13;

    public static final int  STATUS_HEARD_SEND_SUCCESS = 14;

    public static final int  STATUS_HEARD_SEND_ERROR = 15;

    public static final int  STATUS_HEARD_RECEIVE_SUCCESS = 16;

    public static final int  STATUS_HEARD_RECEIVE_ERROR = 17;

    /**
     * Adding the necessary INtent filters for Broadcast receivers
     *
     * @return {@link IntentFilter}
     */
    public static IntentFilter makeNettyClientBroadcastIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED);
        intentFilter.addAction(NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_DATA_RECEIVE);
        intentFilter.addAction(NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_ERROR);
        intentFilter.addAction(NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_DATA_SEND);
        return intentFilter;
    }

    public static void sendBroadcast(Context mContext, String action, Info info) {
        Intent intent = new Intent(action);
        intent.putExtra(action, info);
        mContext.sendBroadcast(intent);
    }

}
