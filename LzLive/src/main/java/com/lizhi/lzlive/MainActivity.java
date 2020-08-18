package com.lizhi.lzlive;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lizhi.live.sdk.LogTag;
import com.lizhi.live.sdk.LzLiveClient;
import com.lizhi.live.sdk.LzLiveClientInfo;
import com.lizhi.live.sdk.LzLiveSession;
import com.lizhi.live.sdk.account.AppAccount;
import com.lizhi.live.sdk.liveflow.FlowInfo;
import com.lizhi.live.sdk.liveflow.LiveCardInfo;
import com.lizhi.live.sdk.liveflow.LiveDynamicIndoCallback;
import com.lizhi.live.sdk.liveflow.LiveDynamicInfo;
import com.lizhi.live.sdk.liveflow.LiveFlowCallback;
import com.lizhi.live.sdk.liveflow.UserDoingThingInfo;
import com.lizhi.lzlive.events.NeedBindPhoneEvent;
import com.lizhi.lzlive.events.NeedLoginEvent;
import com.lizhi.lzlive.events.RoomStateChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = LogTag.TAG;

    private LoginSessoin mSession = LoginSessoin.getInst();


    private EditText mRefreshLiveEdit;
    private Button mSyncLive;

    private Button mHot;
    private Button mMusic;
    private Button mErciyuan;
    private Button mEmotion;
    private Button mSpeak;
    private Button mSleep;
    private Button mAudioLover;
    private Button mAudioBook;
    private TextView mSelectFlow;

    private Button mRefresh;
    private Button mMore;

    private Button mEnterRoom;

    private EditText mEtInputLiveId;

    public static TextView mTvThirdState;
    public static TextView mTvThirdOpenId;
    private Button mBtnThirdLogout;
    private Button mBtnNeedLogin;
    private ImageView mIvBall;
    private long mCurrLiveId;

    private Switch mSWMin;
    private Switch mSWDothing;

    /**
     * 选中的直播标签
     */
    private String mSelectFlowName = "热门";
    private String mSelectFlowType = "0@type";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lz_activity_main);


        mRefreshLiveEdit = findViewById(R.id.et_refresh_live);

        mSyncLive = findViewById(R.id.btn_sync_lives);
        mSyncLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mRefreshLiveEdit.getText().toString())) {
                    return;
                }
                long liveLong = Long.parseLong(mRefreshLiveEdit.getText().toString());
                requestSyncLiveStatus(liveLong);
            }
        });

        initFlowTag();

        mRefresh = findViewById(R.id.btn_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLiveFlow(mSelectFlowType, LzLiveClient.FRESH_TYPE_RESET);
            }
        });

        mMore = findViewById(R.id.btn_more);
        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLiveFlow(mSelectFlowType, LzLiveClient.FRESH_TYPE_MORE);
            }
        });

        mEnterRoom = findViewById(R.id.btn_room);
        mEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mEtInputLiveId.getText().toString())) {
                    return;
                }
                long liveId = Long.parseLong(mEtInputLiveId.getText().toString());
                enterRoom(liveId);
            }
        });

        mEtInputLiveId = findViewById(R.id.et_input_liveid);

        mIvBall = findViewById(R.id.iv_ball);
        mIvBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LzLiveClient.getDefault().enterRoom(mSession.getOpenId(), MainActivity.this, mCurrLiveId);
            }
        });
        mSWMin = findViewById(R.id.switch_min);
        mSWMin.setOnCheckedChangeListener((buttonView, isChecked) -> LzLiveClient.getDefault().setNeedMinRoom(isChecked));

        mSWDothing = findViewById(R.id.switch_dothing);
        mSWDothing.setOnCheckedChangeListener((buttonView, isChecked) -> LzLiveClient.getDefault().setNeedDothingg(isChecked));

        initThird();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSession.getState() == LoginSessoin.TYPE_UNREGISTER) {
            mTvThirdState.setText(getString(R.string.lz_app_user_state_visitor));
            mTvThirdOpenId.setText(String.format(getString(R.string.lz_app_user_openid), LzLiveSession.DEFAULT_OPENID));
            mBtnThirdLogout.setText(getString(R.string.lz_app_user_gotologin));
            mBtnThirdLogout.setVisibility(View.VISIBLE);
        } else if (mSession.getState() == LoginSessoin.TYPE_REGISTER) {
            mTvThirdState.setText(getString(R.string.lz_app_user_state_visitor));
            mTvThirdOpenId.setText(String.format(getString(R.string.lz_app_user_openid), LzLiveSession.DEFAULT_OPENID));
            mBtnThirdLogout.setText(getString(R.string.lz_app_user_gotologin));
            mBtnThirdLogout.setVisibility(View.VISIBLE);
        } else if (mSession.getState() == LoginSessoin.TYPE_LOGIN) {
            mTvThirdState.setText(getString(R.string.lz_app_user_state_login));
            mTvThirdOpenId.setText(String.format(getString(R.string.lz_app_user_openid),  mSession.getOpenId()));
            mBtnThirdLogout.setText(getString(R.string.lz_app_user_logout));
            mBtnThirdLogout.setVisibility(View.VISIBLE);
        }
    }

    private void initThird() {
        mTvThirdState = findViewById(R.id.tv_third_state);
        mTvThirdOpenId = findViewById(R.id.tv_third_openId);

        mBtnThirdLogout = findViewById(R.id.btn_third_loout);
        mBtnThirdLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSession.setAvatarUrl("");
                mSession.setOpenId(LzLiveSession.DEFAULT_OPENID);
                mSession.setGender(LoginSessoin.GENDER_FEMALE);
                mSession.setNickName("");
                mSession.setState(LoginSessoin.TYPE_REGISTER);
                AppAccount.getInstance().doOnLogout();
                LoginActivity.gotoLogin(MainActivity.this, LoginActivity.INDEX_PAGE, false);
                ShareUtil.put(LoginActivity.LAST_LOGIN_OPENID, "");
                finish();
            }
        });

        mBtnNeedLogin = findViewById(R.id.btn_need_login);
        mBtnNeedLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppAccount.getInstance().isLogin()) {
                    AppAccount.getInstance().pleaseLogin();
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.lz_app_user_success), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 可供选择的直播标签tag：热门、音乐、二次元等
     */
    private void initFlowTag() {
        mHot = findViewById(R.id.btn_hot);
        mMusic = findViewById(R.id.btn_music);
        mErciyuan = findViewById(R.id.btn_erciyaun);
        mEmotion = findViewById(R.id.btn_emotion);
        mSpeak = findViewById(R.id.btn_speak);
        mSleep = findViewById(R.id.btn_sleep);
        mAudioLover = findViewById(R.id.btn_audio_lover);
        mAudioBook = findViewById(R.id.btn_audio_book);
        mSelectFlow = findViewById(R.id.tv_select_flow);

        mHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "0@type";
                mSelectFlowName = "热门";
                mSelectFlow.setText(mSelectFlowName);
            }
        });
        mMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1025@type";
                mSelectFlowName = "音乐";
                mSelectFlow.setText(mSelectFlowName);
            }
        });
        mErciyuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1027@type";
                mSelectFlowName = "二次元";
                mSelectFlow.setText(mSelectFlowName);
            }
        });

        mEmotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1026@type";
                mSelectFlowName = "情感";
                mSelectFlow.setText(mSelectFlowName);
            }
        });

        mSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1029@type";
                mSelectFlowName = "脱口秀";
                mSelectFlow.setText(mSelectFlowName);
            }
        });

        mSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1036@type";
                mSelectFlowName = "助眠";
                mSelectFlow.setText(mSelectFlowName);
            }
        });

        mAudioLover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1048@type";
                mSelectFlowName = "声音恋人";
                mSelectFlow.setText(mSelectFlowName);
            }
        });

        mAudioBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectFlowType = "1049@type";
                mSelectFlowName = "有声书";
                mSelectFlow.setText(mSelectFlowName);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 请求直播流
     * @param exId 标签tag：热门、音乐、二次元等
     * @param freshType 刷新方式 1：数据刷新 2：下一页刷新（每页数据10条）
     */
    private void requestLiveFlow(String exId, int freshType) {
        Log.e(TAG, "requestLiveFlow exId " + exId + " exName : " + mSelectFlowName + " freshType : " + freshType);
        //"0@type"
        LzLiveClient.getDefault().requestLiveFlow(mSession.getOpenId(), exId, freshType, new LiveFlowCallback() {
            @Override
            public void onLiveFlow(FlowInfo flowInfo, int error, String msg) {
                Log.e(TAG, "flowInfo.liveCards.size() : " + flowInfo.liveCards.size() + " exId " + exId + " exName : " + mSelectFlowName + " freshType : " + freshType);
                for (LiveCardInfo info : flowInfo.liveCards) {
                    mEtInputLiveId.setText("" + info.liveId);
                    mRefreshLiveEdit.setText("" + info.liveId);
                    Log.e(TAG, "LiveCardInfo info.liveId : " + info.liveId + " info.state : " + info.state + " info.listeners : " + info.listeners
                            + " info.title : " + info.title + " info.subtitle : " + info.subtitle
                            + " info.startTime : " + info.startTime + " info.tag : " + info.tag);
                    Log.e(TAG, "LiveCardInfo info.image : " + info.image);
                    Log.e(TAG, "\n \n");
                }

                for (UserDoingThingInfo info : flowInfo.userDoingThings) {
                    Log.e(TAG, "UserDoingThingInfo info.title : " + info.title + " info.subtitle : " + info.subtitle + " info.action : " + info.action);
                    Log.e(TAG, "\n \n");
                }

                Log.e(TAG, " flowInfo.exId : " + flowInfo.exId);
                Log.e(TAG, " flowInfo.mIsLastPage : " + flowInfo.mIsLastPage);
            }
        });
    }


    /**
     *  刷新直播流房间的状态
     * @param liveId 想要刷新的直播间的lievId
     */
    private void requestSyncLiveStatus(long liveId) {
        Log.e(TAG, "requestSyncLiveStatus liveId : " + liveId);
        List<Long> lives = new ArrayList<>();
        lives.add(liveId);
        LzLiveClient.getDefault().requestSyncLiveStatus(mSession.getOpenId(), lives, new LiveDynamicIndoCallback() {
            @Override
            public void onSyncLiveStatus(List<LiveDynamicInfo> infos) {
                Log.e(TAG, "infos size : " + infos.size());
                for (LiveDynamicInfo info : infos) {
                    Log.e(TAG, "LiveDynamicInfo info.liveId : " + info.liveId + " info.state : " + info.state + " info.listeners : " + info.listeners);
                }
            }
        });
    }

    /**
     * 进入直播间
     * @param liveId
     */
    private void enterRoom(long liveId) {
        LzLiveClient.getDefault().enterRoom(mSession.getOpenId(), MainActivity.this, liveId);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNeedLoginEvent(NeedLoginEvent event) {
        /**
         * 如果用户之前未在SDK内注册，则此时needClientInfo为true，app方需要根据自己的登录状态给SDK传递用户信息与openId
         * 如果用户之前已经在SDK注册，则此时needClientInfo为false，app方需要根据自己的登录状态给SDK传递openId
         */
        if (event.needClientInfo) {

            if (mSession.getState() == LoginSessoin.TYPE_LOGIN) {
                /**
                 * app内用户已经登录，则传用户信息给SDK
                 */
                LzLiveClientInfo.Builder builder = LzLiveClientInfo.Builder.newBuilder();
                builder.avatarUrl(LoginSessoin.DEFAULT_AVATAR)
                        .birthday(LoginSessoin.DEFAULT_BIRTHDAY)
                        .gender(mSession.getGender())
                        .nickName(mSession.getNickName());
                LzLiveClient.getDefault().login(mSession.getOpenId(), builder.build());
            } else {
                /**
                 * 用户未注册/登录，则跳转到登录页面去登录/注册，登录/注册完成后，调用LzLiveClient#login注册SDK
                 */
                LoginActivity.gotoLogin(MainActivity.this, LoginActivity.INDEX_PAGE, true);
            }
        } else {
            if (mSession.getState() == LoginSessoin.TYPE_LOGIN) {
                /**
                 * app内用户已经登录，则使用openId登录SDK
                 */
                LzLiveClient.getDefault().login(mSession.getOpenId(), null);
            } else {
                /**
                 * app内用户未登录，则跳转到登录页面登录，登录完成后，调用LzLiveClient#login登录SDK
                 */
                LoginActivity.gotoLogin(MainActivity.this, LoginActivity.INDEX_PAGE, true);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNeedBindPhoneEvent(NeedBindPhoneEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomStateChangeEvent(RoomStateChangeEvent event) {
        mCurrLiveId = event.liveId;
        if (event.isMinimized && (event.liveState == 0 || event.liveState == 1)) {
            mIvBall.setVisibility(View.VISIBLE);
            Glide.with(MainActivity.this).load(event.avatarUrl).into(mIvBall);
        } else {
            mIvBall.setVisibility(View.GONE);
        }
    }

}
