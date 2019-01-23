package com.erobbing.adb_config_demo.sdk.local_config_base;

import android.content.Context;
import android.os.PowerManager;

import com.erobbing.adb_config_demo.sdk.parse.ParseException;
import com.erobbing.adb_config_demo.sdk.parse.ParserListener;
import com.erobbing.adb_config_demo.sdk.utils.HexTools;
import com.erobbing.adb_config_demo.sdk.utils.MyLogger;
import com.erobbing.adb_config_demo.sdk.utils.MyWakeUp;
import com.erobbing.adb_config_demo.sdk.utils.StartableObject;
import com.erobbing.adb_config_demo.sdk.utils.SysUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingDeque;

import static java.lang.System.exit;


/**
 * Created by quhuabo on 2017/7/3 0003.
 * 用来监听 基于USB的Socket端口，接收PC端的指令：获取参数，设置参数等等
 */

public abstract class LocalConfigServer extends StartableObject implements ParserListener, LocalConfigServerI {

    // 由实现类设置端口号
    protected int mListenPort = 0;

    private Thread mThread = null;
    private volatile boolean mStopFlag = false;
    private MyLogger mLog = MyLogger.jLog();
    private LocalConfigParser mParser = new LocalConfigParser();

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private Context mContext;
    private Socket mSocket;
    DataOutputStream mOutStream;


    @Override
    protected void onStart() {
        synchronized (LocalConfigServer.class) {
            starThread();
        }
    }

    @Override
    protected void onStop() {
        synchronized (LocalConfigServer.class) {
            stopThread();

        }
    }

    private void starThread() {

        if (mThread != null) {
            stopThread();
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                doThreadRoutine();
            }
        });
        mStopFlag = false;
        mThread.start();
    }

    private void stopThread() {
        mLog.i("stopThread ... ");
        if (mThread != null) {
            mStopFlag = true;
            try {
                mThread.join(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                mThread.interrupt();
                mLog.i("退出LocalConfigServer线程");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }

    static class LocalSendData {
        PackMsg m;
        int errCode;
        String text;
    }

    private static LinkedBlockingDeque<LocalSendData> m_sendList = new LinkedBlockingDeque<LocalSendData>();

    /**
     * quhuabo
     * 异步线程向客户端发送数据时，需要调用此函数
     *
     * @param m       将要发送的数据
     * @param errCode
     * @param text    不参与网络传输，仅用于日志调试。
     */
    public static void pushToSendList(PackMsg m, int errCode, String text) {
        LocalSendData x = new LocalSendData();
        x.m = m;
        x.errCode = errCode;
        x.text = text;
        if (m_sendList.size() < 10) {
            //m_sendList.takeLast(); // 去掉一个
            m_sendList.offer(x);
        }

    }

    private void doThreadRoutine() {
        byte[] buffer = new byte[4096];

        mParser.setSyncListener(this);
        mParser.resetAll();

        mLog.i("进入LocalConfigServer线程");
        try {
            for (; !mStopFlag; SysUtil.sleepWhile(2000)) {
                try {
                    ServerSocket server = null;
                    try {
                        server = new ServerSocket(mListenPort);
                        server.setReuseAddress(true);
                        mLog.i("已启动监听：" + mListenPort);
                        //创建一个ServerSocket在端口4700监听客户请求
                    } catch (Exception e) {
                        //出错，打印出错信息
                        mLog.e("Socket监听出错(端口号：" + mListenPort + ")：" + e);
                        e.printStackTrace();
                        try {
                            exit(-1);
                        } catch (Exception _e) {

                        }
                        continue;
                    }
                    try {
                        Socket socket = null;
                        try {
                            socket = server.accept();
                            mLog.i("接收到客户连接");
                            //使用accept()阻塞等待客户请求，有客户
                            //请求到来则产生一个Socket对象，并继续执行
                        } catch (Exception e) {
                            //出错，打印出错信息
                            mLog.e("Socket Accept 出错(端口号：" + mListenPort + ")：" + e);
                            e.printStackTrace();
                            continue;
                        }

                        PowerManager.WakeLock wl = MyWakeUp.wakeLock(mContext);
                        try {

                            mSocket = socket;
                            socket.setSoTimeout(100);

                            m_sendList.clear();

                            String line;
                            DataInputStream is = new DataInputStream(socket.getInputStream());
                            try {
                                //由Socket对象得到输入流，并构造相应的BufferedReader对象
                                mOutStream = new DataOutputStream(socket.getOutputStream());

                                // 发送一包 version_report 数据

                                /*PackMsg msgTemp = new PackMsg();
                                msgTemp.sn = 0;
                                msgTemp.cmd = ccVersionReport;

                                doGetParam(msgTemp);*/

                                int nReadCnt = 0;
                                try {
                                    //从标准输入读入一字符串
                                    int nRead = 0;
                                    while (!mStopFlag) {
                                        try {

                                            nRead = is.read(buffer);
                                            mLog.d("Socket rcv:" + nRead + " bytes");
                                            if (nRead > 0) {
                                                nReadCnt = 0;
                                                mParser.parse(ArrayUtils.subarray(buffer, 0, nRead));
                                            } else {
                                                mLog.w("连接中断.");

                                                break;
                                            }
                                        } catch (ParseException e) {
                                            mLog.i("解析上位机数据错误");
                                        } catch (SocketTimeoutException e) {
                                            //mLog.w("超时...");
                                            nReadCnt++;
                                            if (nReadCnt >= 60 * 10) {
                                                nReadCnt = 0;

                                                mLog.i("localConfigServer 发送一心跳包数据，检测是否网络正常");
                                                PackMsg heartPack = new PackMsg();
                                                heartPack.sn = 0;
                                                heartPack.cmd = 0;
                                                heartPack.data = new byte[1];
                                                _write_to_socket(heartPack, "localConfigServer心跳");

                                            }
                                        }

                                        LocalSendData p = m_sendList.poll();
                                        if (p != null) {
                                            _write_to_socket(p.m, p.text);
                                        }
                                    }  //继续循环
                                } finally {
                                    try {
                                        mOutStream.close(); //关闭Socket输出流
                                        mOutStream = null;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } finally {
                                try {
                                    is.close(); //关闭Socket输入流
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } finally {
                            try {
                                socket.close(); //关闭Socket
                                socket = null;
                                mSocket = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            MyWakeUp.releaseDelay(wl, 10 * 1000);
                        }
                    } finally {
                        try {
                            server.close(); //关闭ServerSocket
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("Error:" + e);
                    //出错，打印出错信息
                }
            }

        } catch (Exception e) {
            mLog.i("LocalConfigServer线程执行出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            mLog.w("退出 LocalConfigServer线程函数");
        }
    }

    @Override
    public void onGotPackage(byte[] packData) {
        try {
            PackMsg msg = new PackMsg();
            msg.fromBuffer(packData);

            doWithPack(msg);

        } catch (Exception e) {
            mLog.w("异常：" + e.getMessage() + " \n" + HexTools.byteArrayToHexReadable(packData));
            e.printStackTrace();
        }
    }


    /**
     * 由线程内部调用
     * 禁止其他外部函数调用
     *
     * @param srcMsg
     * @param textmsg
     */
    private void _write_to_socket(PackMsg srcMsg, String textmsg) {
        try {
            byte[] resp = srcMsg.toBuffer();
            mLog.i("[" + textmsg + "]返回响应数据：" + HexTools.byteArrayToHex(resp));
            mOutStream.write(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回通用消息，通常用来返回错误码，或者不包含除错误码之外的其他消息内容的应答消息
     *
     * @param srcMsg
     * @param errorCode
     * @param textmsg
     */
    private void send_resp_common(PackMsg srcMsg, int errorCode, String textmsg) {

        PackMsg pm = new PackMsg();
        pm.sn = srcMsg.sn;
        pm.cmd = srcMsg.cmd;

        // 放入一些数据
        // pm.data = c.toByteArray();
        errorCode = 0;
        pushToSendList(pm, errorCode, textmsg);

    }


}