package com.tqw.android.webservice;

import com.tqw.android.webservice.define.FailTypes.FailType;

import org.ksoap2.transport.HttpResponseException;

/**
 * Created by Hohenheim on 2017/1/17.
 */

public interface RequestCompletion {
    void finish(String responseResult);
    void httpError(HttpResponseException e);
    void fail(@FailType int failType, String failMsg);
}