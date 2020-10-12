package com.lizhi.lzlive;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lizhi.live.sdk.LogTag;
import com.lizhi.live.sdk.LzLiveCallback;
import com.lizhi.live.sdk.LzLiveClient;
import com.lizhi.live.sdk.LzLiveClientInfo;
import com.lizhi.lzlive.events.NeedBindPhoneEvent;
import com.lizhi.lzlive.events.NeedLoginEvent;
import com.lizhi.lzlive.events.RoomStateChangeEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 这个用于模拟第三方app登陆流程，注意这里账户与登陆状态都是记录在文件的
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LogTag.TAG;

    public static String LAST_LOGIN_OPENID = "last_login_openid";

    private static String NICK_NAME = "nick_name";
    private static String AVATAR = "avatar";
    /**
     * 是否注册的标志（存文件）
     */
    private static String REGISTER_FLAG="register";

    private LoginSessoin mSessoin = LoginSessoin.getInst();
    private TextView mTvOpenId;
    private EditText mEtOpenId;

    private TextView mTvNickName;
    private EditText mEtNickName;

    private TextView mTvAvater;
    private EditText mEtAvatar;

    private TextView mTvGender;
    private TextView mTvVersion;
    private Button mBtnMale;
    private Button mBtnFemale;
    private Button mBtnUnkown;

    private Button mBtnOk;

    private LinearLayout mLLLoginPanel;

    private Button mBtnGotoLogin;
    private Button mBtnGotoRegister;
    private Button mBtnGotoVisitor;

    public static final int INDEX_PAGE = 1;
    public static final int LOGIN_PAGE = 2;
    public static final int REGISTER_PAGE = 3;
    private static int mPageType = INDEX_PAGE;
    /**
     * 登陆流程是否被Sdk调起的场景，如：在未登录状态直播间里面点击关注、评论、送礼等
     */
    private static boolean mLoginBySdk = false;

    public static void gotoLogin(Activity context, int page, boolean loginBySdk) {
        mPageType = page;
        mLoginBySdk = loginBySdk;
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        Log.d("lzLiveAuth","mLoginBySdk="+mLoginBySdk);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lz_activity_login);

        boolean isSucc = LzLiveClient.getDefault().init(LzLiveApplication.getApplication(), false, false, "eyJzdWJhcHBpZCI6IjE5OTE0MSIsImFwcGtleSI6ImRmOTYyMGFiZjMzZTg3MDFlMThmZTFmMzExM2VkOTI3Iiwib3BlbmlkX3Bvc3RmaXgiOiJfb2ZmaWNhbCJ9");
        if (!isSucc) {
            Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        LzLiveClient.getDefault().registerCallBack(new LzLiveCallback() {
            @Override
            public void onNeedLogin(boolean needClientInfo) {
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
        /**
         * 如果上次是已登录状态则恢复到上次登陆的状态
         */
        if (!TextUtils.isEmpty(ShareUtil.get(LAST_LOGIN_OPENID, ""))) {
            mSessoin.setState(LoginSessoin.TYPE_LOGIN);
            mSessoin.setOpenId(ShareUtil.get(LAST_LOGIN_OPENID, ""));
            String openId = mSessoin.getOpenId();
            mSessoin.setNickName(ShareUtil.get(openId + NICK_NAME, ""));
            mSessoin.setAvatarUrl(ShareUtil.get(openId + AVATAR, ""));
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        mLLLoginPanel = findViewById(R.id.ll_login_panel);


        mTvOpenId = findViewById(R.id.tv_openId);
        mEtOpenId = findViewById(R.id.et_openid);

        mTvNickName = findViewById(R.id.tv_nickname);
        mEtNickName = findViewById(R.id.et_nickname);

        mTvAvater = findViewById(R.id.tv_avatar);
        mEtAvatar = findViewById(R.id.et_avatar);

        mTvGender = findViewById(R.id.tv_gender);
        mBtnMale = findViewById(R.id.btn_male);
        mTvVersion = findViewById(R.id.tv_version);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mTvVersion.setText(String.format(getString(R.string.lz_app_version), packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mBtnMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessoin.setGender(LoginSessoin.GENDER_MALE);
                mTvGender.setText(getString(R.string.lz_user_male));
            }
        });
        mBtnFemale = findViewById(R.id.btn_female);
        mBtnFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessoin.setGender(LoginSessoin.GENDER_FEMALE);
                mTvGender.setText(getString(R.string.lz_user_female));
            }
        });

        mBtnUnkown = findViewById(R.id.btn_unkonw);
        mBtnUnkown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessoin.setGender(LoginSessoin.GENDER_UNKNOW);
                mTvGender.setText(getString(R.string.lz_user_unknow));
            }
        });

        mBtnOk = findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessoin.setOpenId(mEtOpenId.getText().toString());
                String openId = mEtOpenId.getText().toString();
                if (mPageType == REGISTER_PAGE) {
                    if (ShareUtil.get(openId, "").equals(REGISTER_FLAG)) {
                        Toast.makeText(getBaseContext(), getString(R.string.lz_user_registered), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mSessoin.setNickName(mEtNickName.getText().toString());
                    mSessoin.setAvatarUrl(mEtAvatar.getText().toString());

                    mSessoin.setState(LoginSessoin.TYPE_LOGIN);

                    ShareUtil.put(openId, REGISTER_FLAG);
                    ShareUtil.put(openId + NICK_NAME, mSessoin.getNickName());
                    ShareUtil.put(openId + AVATAR, mSessoin.getAvatarUrl());
                    ShareUtil.put(LAST_LOGIN_OPENID, openId);
                    if (mLoginBySdk) {
                        /**
                         * 第三方需要在注册登录这里告诉sdk
                         */
                        LzLiveClientInfo.Builder builder = LzLiveClientInfo.Builder.newBuilder();
                        builder.avatarUrl(LoginSessoin.DEFAULT_AVATAR)
                                .birthday(LoginSessoin.DEFAULT_BIRTHDAY).gender(mSessoin.getGender()).nickName(mSessoin.getNickName());
                        LzLiveClient.getDefault().login(openId, builder.build());
                    }
                } else if (mPageType == LOGIN_PAGE) {
                    if (!ShareUtil.get(openId, "").equals(REGISTER_FLAG)) {
                        Toast.makeText(getBaseContext(), getString(R.string.lz_user_unregister), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mSessoin.setState(LoginSessoin.TYPE_LOGIN);
                    if (mLoginBySdk) {
                        LzLiveClient.getDefault().login(openId, null);
                    }
                }
                if (!mLoginBySdk) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                }

                finish();
            }
        });

        mBtnGotoLogin = findViewById(R.id.btn_login);
        mBtnGotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderLogin();
            }
        });
        mBtnGotoRegister = findViewById(R.id.btn_register);
        mBtnGotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderRegister();
            }
        });
        mBtnGotoVisitor = findViewById(R.id.btn_visitor);
        mBtnGotoVisitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessoin.setState(LoginSessoin.TYPE_UNREGISTER);
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (mPageType == INDEX_PAGE) {
            renderVisitor();
        } else if (mPageType == REGISTER_PAGE) {
            renderRegister();
        } else if (mPageType == LOGIN_PAGE) {
            renderLogin();
        }
    }

    private void renderLogin() {
        mPageType = LOGIN_PAGE;
        mLLLoginPanel.setVisibility(View.VISIBLE);
        mTvNickName.setVisibility(View.GONE);
        mEtNickName.setVisibility(View.GONE);
        mTvAvater.setVisibility(View.GONE);
        mEtAvatar.setVisibility(View.GONE);
        mBtnFemale.setVisibility(View.GONE);
        mBtnUnkown.setVisibility(View.GONE);
        mBtnMale.setVisibility(View.GONE);
        mTvGender.setVisibility(View.GONE);
        mBtnOk.setVisibility(View.VISIBLE);
    }

    private void renderRegister() {
        mPageType = REGISTER_PAGE;
        mLLLoginPanel.setVisibility(View.VISIBLE);
        mTvNickName.setVisibility(View.VISIBLE);
        mEtNickName.setVisibility(View.VISIBLE);
        mTvAvater.setVisibility(View.VISIBLE);
        mEtAvatar.setVisibility(View.VISIBLE);
        mBtnFemale.setVisibility(View.VISIBLE);
        mBtnMale.setVisibility(View.VISIBLE);
        mBtnUnkown.setVisibility(View.VISIBLE);
        mTvGender.setVisibility(View.VISIBLE);
        mBtnOk.setVisibility(View.VISIBLE);
    }

    private void renderVisitor() {
        mPageType = INDEX_PAGE;
        mLLLoginPanel.setVisibility(View.GONE);
        mBtnOk.setVisibility(View.GONE);
    }
}
