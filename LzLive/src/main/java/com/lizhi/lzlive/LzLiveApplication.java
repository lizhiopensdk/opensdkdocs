package com.lizhi.lzlive;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lizhi.live.sdk.LogTag;
import com.lizhi.live.sdk.LzLiveCallback;
import com.lizhi.live.sdk.LzLiveClient;
import com.lizhi.lzlive.events.NeedBindPhoneEvent;
import com.lizhi.lzlive.events.NeedLoginEvent;
import com.lizhi.lzlive.events.RoomStateChangeEvent;

import org.greenrobot.eventbus.EventBus;

public class LzLiveApplication extends Application {

    private static final String TAG = LogTag.TAG;

    private static Application sInstance;
    public static Application getApplication(){
        return sInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ShareUtil.init(this);
        sInstance=this;
    }
}
