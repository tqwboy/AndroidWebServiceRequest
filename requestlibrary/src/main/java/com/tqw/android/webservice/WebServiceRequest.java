package com.tqw.android.webservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Hohenheim on 2017/1/15.
 */

public class WebServiceRequest {
    private ExecutorService mThreadPool;
    private int mPoolSize;

    public WebServiceRequest() {
        mPoolSize = 3;
    }

    public WebServiceRequest(int taskPoolSize) {
        mPoolSize = taskPoolSize<=0 ? 3:taskPoolSize;
    }

    private void initThreadPool() {
        if(mPoolSize == 1)
            mThreadPool = Executors.newSingleThreadScheduledExecutor();
        else
            mThreadPool = Executors.newFixedThreadPool(mPoolSize);
    }

    public void shutdownRequest() {
        if(!mThreadPool.isShutdown())
            mThreadPool.shutdownNow();
    }
}
