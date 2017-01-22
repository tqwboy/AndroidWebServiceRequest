package com.tqw.android.webservice;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.tqw.android.webservice.define.FailTypes;
import com.tqw.android.webservice.define.SoapVers.SoapVer;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Hohenheim on 2017/1/22.
 */
public class RequestManager {
    private static RequestManager instance = null;

    private ExecutorService mThreadPool;
    private int mPoolSize;

    private RequestManager() {
        mPoolSize = 3;
    }

    public static synchronized RequestManager getInstance() {
        if(null == instance) {
            instance = new RequestManager();
        }

        return instance;
    }

    /**
     * WebService请求
     *
     * @param endPoint        WebService服务器地址
     * @param nameSpace       命名空间
     * @param methodName      WebService的调用方法名
     * @param params          WebService的参数集合，可以为null
     * @param timeout         超时时间，单位为毫秒
     * @param isDotNet        是否是DotNet开发的WebService
     * @param soapAction      SOAP指令
     * @param completion      请求结果回调接口
     */
    public void request(String endPoint, String nameSpace, String methodName,
                        SimpleArrayMap<String, String> params, int timeout,
                        boolean isDotNet, String soapAction, @SoapVer int sopaVer,
                        @NonNull RequestCompletion completion) {

        initThreadPool();

        HttpTransportSE transportSE = new HttpTransportSE(endPoint, timeout); //创建HttpTransportSE对象，传递WebService服务器地址
        SoapObject request = new SoapObject(nameSpace, methodName); //创建SoapObject对象用于传递请求参数

        for (int index = 0; null!=params && index<params.size(); ++index) {
            String key = params.keyAt(index);
            String value = params.get(key);
            request.addProperty(key, value);
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(sopaVer); //实例化SoapSerializationEnvelope，传入WebService的SOAP协议的版本号
        envelope.dotNet = isDotNet; //设置是否调用的是.Net开发的WebService
        envelope.setOutputSoapObject(request);

        //提交请求
        RequestExecThread execThread = new RequestExecThread(envelope, transportSE, soapAction, completion);
        mThreadPool.execute(execThread);
    }

    private void initThreadPool() {
        if(null==mThreadPool || mThreadPool.isShutdown()) {
            if(mPoolSize == 1)
                mThreadPool = Executors.newSingleThreadScheduledExecutor();
            else
                mThreadPool = Executors.newFixedThreadPool(mPoolSize);
        }
    }

    //请求线程
    private static class RequestExecThread implements Runnable {
        private RequestCompletion completion;
        private SoapSerializationEnvelope envelope;
        private HttpTransportSE transportSE;
        private String soapAction;

        private Handler handler;
        private final int REQUEST_FINISH = 0x0001;
        private final int REQUEST_HTTP_ERR = 0x0002;
        private final int REQUEST_FAIL = 0x0003;

        public RequestExecThread(SoapSerializationEnvelope envelope,
                                 HttpTransportSE transportSE,
                                 String soapAction,
                                 RequestCompletion completion) {

            this.envelope = envelope;
            this.transportSE = transportSE;
            this.soapAction = soapAction;
            this.completion = completion;
        }

        @Override
        public void run() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                        case REQUEST_FINISH:
                            Object responseObj = msg.obj;
                            String responseStr = null;
                            if(null != responseObj)
                                responseStr = (String) responseObj;
                            completion.finish(responseStr);
                            break;

                        case REQUEST_HTTP_ERR:
                            HttpResponseException exception = (HttpResponseException) msg.obj;
                            completion.httpError(exception);
                            break;

                        case REQUEST_FAIL:
                            String failMsg = (String) msg.obj;
                            completion.fail(msg.arg1, failMsg);
                            break;

                        default:
                            break;
                    }
                }
            };

            Message msg = handler.obtainMessage();

            try {
                transportSE.call(soapAction, envelope);
                Object responseObj = envelope.getResponse();

                msg.what = REQUEST_FINISH;
                msg.obj = responseObj;
            }
            catch (IOException e) {
                e.printStackTrace();

                if(e instanceof SocketTimeoutException
                        || e instanceof UnknownHostException) {
                    //连接超时
                    msg.what = REQUEST_FAIL;
                    msg.obj = e.toString();
                    msg.arg1 = FailTypes.TIMEOUT;
                }
                else if(e instanceof HttpResponseException) {
                    //HTTP错误
                    msg.what = REQUEST_HTTP_ERR;
                    msg.obj = e;
                }
                else {
                    msg.what = REQUEST_FAIL;
                    msg.obj = e.toString();
                    msg.arg1 = FailTypes.IO_EXCEPTION;
                }
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();

                msg.what = REQUEST_FAIL;
                msg.obj = e.toString();
                msg.arg1 = FailTypes.XML_EXCEPTION;
            }

            handler.sendMessage(msg);
        }
    }

    public void stopAllRequest() {
        if(!mThreadPool.isShutdown())
            mThreadPool.shutdownNow();
    }

    public void setPoolSize(int poolSize) {
        if(poolSize==mPoolSize || poolSize<=0)
            return;

        mPoolSize = poolSize;
        stopAllRequest();
    }
}