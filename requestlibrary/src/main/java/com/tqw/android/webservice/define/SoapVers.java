package com.tqw.android.webservice.define;

import android.support.annotation.IntDef;

import org.ksoap2.SoapEnvelope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Hohenheim on 2017/1/22.
 */

public interface SoapVers {
    @IntDef(flag = true, value = {
            SoapEnvelope.VER10,
            SoapEnvelope.VER11,
            SoapEnvelope.VER12
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface SoapVer{}
}
