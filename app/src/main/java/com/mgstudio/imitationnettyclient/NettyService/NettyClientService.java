package com.mgstudio.imitationnettyclient.NettyService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.mgstudio.imitationnettyclient.Broadcast.Info;
import com.mgstudio.imitationnettyclient.Broadcast.NettyClientBroadcastUtil;
import com.mgstudio.imitationnettyclient.Netty.NettyClient;
import com.mgstudio.imitationnettyclient.Netty.NettyClientListener;
import com.mgstudio.imitationnettyclient.Utils.DebugUtils;
import com.mgstudio.imitationnettyclient.Utils.WriteLogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyClientService extends Service implements NettyClientListener {

    private String tag = this.getClass().getSimpleName();
    private NetworkReceiver receiver;
    private static String sessionId = null;
    private static Context mContext;
    private byte[] heardBeat = {(byte) 0XBB, (byte) 0X0B, (byte) 0X01, (byte) 0XBB, (byte) 0XFF, (byte) 0XFF, (byte) 0XBF};

    private ScheduledExecutorService mScheduledExecutorService;  //调度服务

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NettyClient.getInstance().setListener(this);  // 设置监听器
        connect(MyApplication.getContext(),CommandUtil.heardBeatCommand);  // 服务启动 netty client连接
        //连接中广播
        NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED, new Info(NettyClientBroadcastUtil.STATUS_CONNECTING));

        // 自定义心跳
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                NettyClient.getInstance().sendMsgToServer(heardBeat, new ChannelFutureListener() {//发送心跳
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            DebugUtils.debugLog_Timer(tag, "Write heartbeat successful-------->心跳发送成功");
                        } else {
                            DebugUtils.debugLog_Timer(tag, "Write heartbeat error");
                            WriteLogUtil.writeLogByThread("heartbeat error");
                        }
                    }
                });
            }
        }, 1, 5, TimeUnit.SECONDS);
        return START_NOT_STICKY;
    }


    @Override
    public void onServiceStatusConnectChanged(int statusCode) {  // 连接状态监听
        DebugUtils.debugLog_Timber(tag, "connect status:" + statusCode);
        Info info = new Info();
        switch (statusCode) {
            case NettyClientBroadcastUtil.STATUS_CONNECT_SUCCESS:
                authenticData();//数据认证 TODO
                info.setState(NettyClientBroadcastUtil.STATUS_CONNECT_SUCCESS);
                NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED, info);
                break;
            case NettyClientBroadcastUtil.STATUS_CONNECT_CLOSED:
                info.setState(NettyClientBroadcastUtil.STATUS_CONNECT_CLOSED);
                NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED, info);
                break;
            case NettyClientBroadcastUtil.STATUS_CONNECT_ERROR:
                info.setState(NettyClientBroadcastUtil.STATUS_CONNECT_ERROR);
                NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED, info);
                break;
            default:
                info.setState(NettyClientBroadcastUtil.STATUS_EXCEPTION);
                NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED, info);
                WriteLogUtil.writeLogByThread("tcp connect error");
                break;
        }
    }

    /**
     * 认证数据请求
     */
    private void authenticData() {
        AuthModel auth = new AuthModel();
        auth.setI(1);
        auth.setU("sn");
        auth.setN("name");
        auth.setF("1");
        auth.setT((int) (System.currentTimeMillis() / 1000));
        //构造响应
        byte[] content = CommandUtil.getEncryptBytes(auth);

        NettyClient.getInstance().sendMsgToServer(content, new ChannelFutureListener() {//发送响应
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    DebugUtils.debugLog_Timer(tag, "Write auth successful");
                    //发送数据广播
                    Info info = new Info();
                    info.setState(NettyClientBroadcastUtil.STATUS_SEND_SUCCESS);
                    NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_DATA_SEND, info);
                } else {
                    DebugUtils.debugLog_Timer(tag, "Write auth error");
                    WriteLogUtil.writeLogByThread("tcp auth error");
                    //发送数据广播
                    Info info = new Info();
                    info.setState(NettyClientBroadcastUtil.STATUS_SEND_ERROR);
                    NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_DATA_SEND, info);
                }
            }
        });
    }


    @Override
    public void onMessageResponse(ByteBuf byteBuf) {//通信信息处理

        byte[] bytes = byteBuf.array();

        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        DebugUtils.debugLog(tag, "tcp receive bytes:" + Arrays.toString(req));
        DebugUtils.debugLog(tag, "tcp receive bytes:" + ByteUtil.bytesToHex(req));
        DebugUtils.debugLog(tag, "tcp receive bytes:" + ByteUtil.byteToASCII(req));
//        DebugUtils.debugLog_Timer(tag, "tcp receive data:" + ByteUtil.bytesToHex(bytes));

        //接收数据广播
        Info info = new Info();
        info.setState(NettyClientBroadcastUtil.STATUS_RECEIVE_SUCCESS);
        info.setBytes(bytes);
        info.setContent(byteBuf.toString());
        NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_DATA_RECEIVE, info);

        //接收**********************************************************************************************
        if (0xED == ByteUtil.unsignedByteToInt(bytes[0]) && 0xFE == ByteUtil.unsignedByteToInt(bytes[1])) {
            if (1 == bytes[2]) {
                int cardinal = (int) ByteUtil.unsigned4BytesToInt(bytes, 5);
                int realLen = cardinal + 9;
                int len = byteBuf.writerIndex();
                // 接收到的数据有可能会粘包，只需要判断数据的长度大于或者等于真实的长度即可
                if (len >= realLen) {
                    int word = ByteUtil.bytesToShort(ByteUtil.subBytes(bytes, 3, 2));
                    if (word == 1001) {
                        byte[] data = new byte[cardinal];
                        System.arraycopy(bytes, 9, data, 0, data.length);
                        Blowfish blowfish = new Blowfish();
                        String result = new String(blowfish.decryptByte(data));
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            sessionId = jsonObject.getString("s");
                        } catch (JSONException e) {
                            DebugUtils.debugLog_Timer(tag, e.getMessage());
                        }
                    } else if (word == 2002) {
                        byte[] data = new byte[cardinal];
                        System.arraycopy(bytes, 9, data, 0, data.length);
                        Blowfish blowfish = new Blowfish();
                        String result = new String(blowfish.decryptByte(data));
                        DebugUtils.debugLog_Timer(tag, result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            handle(word, jsonObject.getInt("i"), jsonObject.getInt("r"));
                        } catch (JSONException e) {
                            DebugUtils.debugLog_Timer(tag, e.getMessage());
                        }
                    } else {
                        String log = "undefined request type";
                        DebugUtils.debugLog_Timer(tag, log);
                        WriteLogUtil.writeLogByThread(log);
                    }
                } else {
                    String log = String.format("request byte array content length inequality, realLen=%d, len=%d", realLen, len);
                    DebugUtils.debugLog_Timer(tag, log);
                    WriteLogUtil.writeLogByThread(log);
                }
            } else if (5 == bytes[2]) {
                DebugUtils.debugLog_Timer(tag, "heartbeat");
            }
            //响应**********************************************************************************************
        } else if (0xFE == ByteUtil.unsignedByteToInt(bytes[0])
                && 0xED == ByteUtil.unsignedByteToInt(bytes[1])
                && 0xFE == ByteUtil.unsignedByteToInt(bytes[2])) {
            if (1 == bytes[3]) {
                // 忽略bytes[4],bytes[5]。作用是接口升级
                int cardinal = (int) ByteUtil.unsigned4BytesToInt(bytes, 8);
                int len = byteBuf.writerIndex();
                // 前12个字节是请求头，后4个字节是校验值
                int realLen = cardinal + 12 + 4;
                // 返回的数据有可能会粘包，只需要判断数据的长度大于或者等于真实的长度即可
                if (len >= realLen) {
                    int word = ByteUtil.bytesToShort(ByteUtil.subBytes(bytes, 6, 2));
                    if (word == 2001) {
                        byte[] data = new byte[cardinal];
                        System.arraycopy(bytes, 12, data, 0, data.length);
                        byte[] crc32 = new byte[4];
                        System.arraycopy(bytes, realLen - 4, crc32, 0, crc32.length);
                        // 对内容进行CRC校验
                        if (CRC32Util.getCRC32Long(data) == ByteUtil.unsigned4BytesToInt(crc32, 0)) {
                            Blowfish blowfish = new Blowfish();
                            String result = new String(blowfish.decryptByte(data));
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                int i = jsonObject.getInt("i");
                                if (sessionId == null) {
                                    WriteLogUtil.writeLogByThread("sessionId is null");
                                    authenticData();
                                    handle(word, i, 0);
                                    return;
                                }
                                byte[] session = sessionId.getBytes();
                                byte[] sign = "WiseUC@2016".getBytes();
                                byte[] content = new byte[session.length + sign.length];
                                System.arraycopy(session, 0, content, 0, session.length);
                                System.arraycopy(sign, 0, content, session.length, sign.length);

                                // 对Session ID进行CRC校验
                                if (jsonObject.getLong("c") == CRC32Util.getCRC32(content)) {
                                    handle(word, i, 1);
                                } else {
                                    String log = "open the door session id crc32 verification failure";
                                    DebugUtils.debugLog_Timer(tag, log);
                                    WriteLogUtil.writeLogByThread(log);
                                }
                            } catch (JSONException e) {
                                DebugUtils.debugLog_Timer(tag, e.getMessage());
                            }
                        } else {
                            String log = "open the door crc32 data verification failure";
                            DebugUtils.debugLog_Timer(tag, log);
                            WriteLogUtil.writeLogByThread(log);
                        }
                    } else {
                        String log = "undefined response type";
                        DebugUtils.debugLog_Timer(tag, log);
                        WriteLogUtil.writeLogByThread(log);
                    }
                } else {
                    String log = String.format("response byte array content length inequality, realLen=%d, len=%d", realLen, len);
                    DebugUtils.debugLog_Timer(tag, log);
                    ;
                    WriteLogUtil.writeLogByThread(log);
                }
            } else if (5 == bytes[3]) {
                DebugUtils.debugLog_Timer(tag, "heartbeat");
            }
        } else {
            DebugUtils.debugLog_Timer(tag, "unknown");
            WriteLogUtil.writeLogByThread("unknown");
        }
    }

    @Override
    public void onMessageComplete(ChannelHandlerContext cxt) {

    }

    @Override
    public void onuserEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED,
                        new Info(NettyClientBroadcastUtil.STATUS_CONNECT_ERROR));
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                try {
                    byte[] requestBody = {(byte) 0xEE, (byte) 0x00};
                    NettyClient.getInstance().sendMsgToServer(requestBody, new ChannelFutureListener() {//发送心跳
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                DebugUtils.debugLog_Timer(tag, "onuserEventTriggered-Write heartbeat successful");
                            } else {
                                DebugUtils.debugLog_Timer(tag, "onuserEventTriggered-Write heartbeat error");
                                WriteLogUtil.writeLogByThread("onuserEventTriggered-heartbeat error");
                            }
                        }
                    });
                    NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_DATA_SEND,
                            new Info(NettyClientBroadcastUtil.STATUS_HEARD_SEND_SUCCESS));
                } catch (Exception e) {
                    DebugUtils.debugLog_Timer(tag, e.getMessage());
                }
            }
        }
    }

    private void handle(int t, int i, int f) {
        // TODO 实现自己的业务逻辑
    }

    private void connect(Context context, byte[] heardBeat) {
        if (!NettyClient.getInstance().getConnectStatus()) {
            mContext = context;
            //启动连接广播
            NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_STATE_CHANGED, new Info(NettyClientBroadcastUtil.STATUS_CONNECTING));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            }).start();
        }
        this.heardBeat = heardBeat;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        heardBeatShutdown();
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();
    }

    private void heardBeatShutdown() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }

    public byte[] getHeardBeat() {
        return heardBeat;
    }

    public void setHeardBeat(byte[] heardBeat) {
        this.heardBeat = heardBeat;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //网络监听
    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                    connect(MyApplication.getContext());
                    //网络异常
                    NettyClientBroadcastUtil.sendBroadcast(mContext, NettyClientBroadcastUtil.ACTION_NETTY_SOCKET_ERROR,
                            new Info(NettyClientBroadcastUtil.STATUS_EXCEPTION_INTERNET));
                }
            }
        }
    }


}
