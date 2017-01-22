package com.tqw.android.webservice.define;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Hohenheim on 2017/1/22.
 */

public interface FailTypes {
    @IntDef(flag = true, value = {
            TIMEOUT,
            XML_EXCEPTION,
            IO_EXCEPTION
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface FailType{}

    public static int TIMEOUT = 1;
    public static int XML_EXCEPTION = 2;
    public static int IO_EXCEPTION = 3;
}