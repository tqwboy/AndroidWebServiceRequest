package com.tqw.android.webservice;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.tqw.android.webservice.define.SoapVers.SoapVer;

import org.ksoap2.SoapEnvelope;

/**
 * Created by Hohenheim on 2017/1/15.
 */

public class WebServiceRequest {
    public String endPoint;
    public String nameSpace;
    public String methodName;
    public SimpleArrayMap<String, String> params;
    public int timeout;
    public boolean isDotNet;
    public String soapAction;
    @SoapVer public int soapVer;
    private RequestCompletion completion;

    private WebServiceRequest() {
        timeout = 10 * 1000; //默认超时时间为10秒
        isDotNet = false;
        soapVer = SoapEnvelope.VER10;
    }

    public static synchronized WebServiceRequest create(@NonNull RequestCompletion completion) {
        WebServiceRequest request = new WebServiceRequest();
        request.completion = completion;
        return request;
    }

    public WebServiceRequest endPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public WebServiceRequest nameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public WebServiceRequest methodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public WebServiceRequest params(SimpleArrayMap<String, String> params) {
        this.params = params;
        return this;
    }

    /**
     * 设置请求超时时间
     * @param timeout 请求超时时间，单位：毫秒
     */
    public WebServiceRequest timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * WebService是否是DotNet平台
     * @param isDotNet true：WebService是DotNet平台；false：WebService不是DotNet平台
     */
    public WebServiceRequest isDotNet(boolean isDotNet) {
        this.isDotNet = isDotNet;
        return this;
    }

    public WebServiceRequest soapAction(String soapAction) {
        this.soapAction = soapAction;
        return this;
    }

    public WebServiceRequest soapVer(@SoapVer int soapVer) {
        this.soapVer = soapVer;
        return this;
    }

    public boolean execRequest() {
        boolean execRequestSuccess = false;

        if(!TextUtils.isEmpty(endPoint) && !TextUtils.isEmpty(nameSpace)
                && !TextUtils.isEmpty(methodName)) {

            RequestManager requestManager = RequestManager.getInstance();
            requestManager.request(endPoint, nameSpace, methodName, params, timeout, isDotNet,
                    soapAction, soapVer, completion);

            execRequestSuccess = true;
        }

        return execRequestSuccess;
    }
}