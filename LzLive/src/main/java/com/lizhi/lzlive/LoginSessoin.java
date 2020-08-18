package com.lizhi.lzlive;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lizhi.live.sdk.LzLiveSession;

public class LoginSessoin {

    /**
     * 登录状态
     */
    public static final int TYPE_UNREGISTER = -1;
    public static final int TYPE_REGISTER = 0;
    public static final int TYPE_LOGIN = 1;

    /**
     * 性别
     */
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final int GENDER_UNKNOW = 2;

    public static final String DEFAULT_AVATAR = "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1629672413,2077090730&fm=26&gp=0.jpg";

    public static final long DEFAULT_BIRTHDAY = 1027424052000L;

    private volatile static LoginSessoin mSession;

    private String openId = LzLiveSession.DEFAULT_OPENID;
    //0:regisetr 1:login
    private int state = TYPE_UNREGISTER;
    private String nickName;
    private String avatarUrl;
    private int gender;

    private LoginSessoin() {
    }

    public static LoginSessoin getInst() {
        if (mSession == null) {
            synchronized (LoginSessoin.class) {
                mSession = new LoginSessoin();
            }
        }
        return mSession;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}
