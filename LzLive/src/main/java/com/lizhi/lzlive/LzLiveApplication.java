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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ShareUtil.init(this);
        /**
         * 初始化sdk
         */
        LzLiveClient.getDefault().init(this, false, false, "pnhien39yo45053e8701e18fedine89h3iejbji");
        LzLiveClient.getDefault().registerCallBack(new LzLiveCallback() {
            @Override
            public void onNeedLogin(boolean needClientInfo) {
                /**
                 * 需要第三方app登陆的回调
                 */
                Log.e(TAG, "onNeedLogin needClientInfo : " + needClientInfo);
                NeedLoginEvent event = new NeedLoginEvent();
                event.needClientInfo = needClientInfo;
                EventBus.getDefault().post(event);

            }

            @Override
            public String onNeedBindPhone() {
                Log.e(TAG, "onNeedLogin onNeedBindPhone ");
                /**
                 * 用户绑定手机号需要app方提前准备好，以便此接口回调时能够及时拿到，
                 * 如果没有，SDK将调起内部的绑定流程
                 */
                NeedBindPhoneEvent event = new NeedBindPhoneEvent();
                EventBus.getDefault().post(event);
                if (!TextUtils.isEmpty(ShareUtil.get(ShareUtil.PHONE_NUM, ""))) {
                    return ShareUtil.get(ShareUtil.PHONE_NUM, "");
                }
                return null;
            }

            @Override
            public void onRoomState(boolean isMinimized, long liveId, int liveState, String avatarUrl) {
                /**
                 * 直播间状态的回调：只有开启了最小化开关才会有此回调
                 */
                Log.d(TAG,String.format("isMinimized=%b, liveId=%d, liveState=%d, avatarUrl=%s", isMinimized, liveId, liveState, avatarUrl));
                RoomStateChangeEvent event = new RoomStateChangeEvent();
                event.isMinimized = isMinimized;
                event.liveId = liveId;
                event.liveState = liveState;
                event.avatarUrl = avatarUrl;
                EventBus.getDefault().post(event);
            }

            @Override
            public void onError(int error, String msg) {
                Log.e(TAG, "onError error : " + error + " msg : " + msg);
                Toast.makeText(getApplicationContext(), "sdk注册失败 ： " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
