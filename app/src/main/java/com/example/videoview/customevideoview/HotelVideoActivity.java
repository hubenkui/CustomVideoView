package com.example.videoview.customevideoview;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;


/**
 * Created by bkhu on 17/4/7.
 */
public class HotelVideoActivity extends FragmentActivity implements View.OnClickListener, View.OnTouchListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    public VideoView mVideoView;
    public HotelCustomMediaController mMediaController;
    public int width;
    public int height;

    private int mCurrentPosition;

    private String mHotelVideo;
    private RelativeLayout mCloseLayout;

    private boolean isShowHotelInfo = true;
    private VideoHandler mHandler = new VideoHandler();
    private ImageView mVideoImageBg;
    private String mErrorType;
    private RelativeLayout mVideoLayout;

    private ImageView mHotelVideoPlayCompleted;
    private RelativeLayout mVideoViewLayout;
    private LinearLayout mVideoVolumeLayout;
    private ImageView mVolumeIcon;
    private TextView mVolumeStatusText;

    private boolean mVolumeStatus = false;
    private AudioManager mAudioManager;
    private int mCurrentVolume;
    private FrameLayout mNoWifiFrameLayout;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_video_activity_layout);
        initView();
        initHotelInfoData();
        initVideoPlayerLayout();
        initVolume();

    }

    private void initVolume() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Object volumeStatus = Session.getSessionInstance().getAttribute("volumeOpenStatus");
        if (volumeStatus == null) {
            mVolumeStatusText.setText("打开音量");
            mVolumeIcon.setBackground(getResources().getDrawable(R.drawable.hotel_vedio_volume_mute_icon));
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        } else {
            boolean currentVolumeStatus = (boolean) volumeStatus;
            showVolumeStatus(currentVolumeStatus);
        }
    }

    private void showVolumeStatus(boolean isNeedShow) {
        mVolumeStatusText.setText(isNeedShow ? "关闭音量" : "打开音量");
        mVolumeIcon.setBackground(isNeedShow ? getResources().getDrawable(R.drawable.hotel_video_volume_louder_icon) : getResources().getDrawable(R.drawable.hotel_vedio_volume_mute_icon));
    }

    private void refreshVolume() {
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        showVolumeStatus(mCurrentVolume > 0);
        Log.e("volume", "currentVolume" + mCurrentVolume);
    }

    private void showNoWifiDialog() {
        HotelVideoNoWifiFragment videoNoWifiFragment = HotelVideoNoWifiFragment.newInstance(mErrorType, "0");

        videoNoWifiFragment.setStartPlayVideoCallBack(new HotelVideoNoWifiFragment.IStartPlayVideoCallBack() {
            @Override
            public void startPlayVideo(String netType) {
                if (HotelVideoConstants.WIFI_NOT_AVAILABLE.equals(netType)) {
                    isShowHotelInfo = true;
                    startVideoPlayer();
                } else {

                    refreshBgAnimal();
                    videoReadyPlay();

                }
            }
        });
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.add(R.id.no_wifi_tip_layout, videoNoWifiFragment, "hotel_video_no_wifi");
        ft.addToBackStack("hotel_video_no_wifi");
        ft.show(videoNoWifiFragment);
        ft.commitAllowingStateLoss();
    }

    public void refreshBgAnimal() {
        mNoWifiFrameLayout.setVisibility(View.VISIBLE);
        mNoWifiFrameLayout.setAlpha(1.0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.start();
        mNoWifiFrameLayout.startAnimation(alphaAnimation);

    }


    private void initView() {
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mVideoView = (VideoView) findViewById(R.id.video);
        mVideoImageBg = (ImageView) findViewById(R.id.video_screen_bg);
        mCloseLayout = (RelativeLayout) findViewById(R.id.video_close_layout);
        mHotelVideoPlayCompleted = (ImageView) findViewById(R.id.video_play_completed_icon);

        mCloseLayout.setOnClickListener(this);
        mHotelVideoPlayCompleted.setOnClickListener(this);
        mVideoViewLayout = (RelativeLayout) findViewById(R.id.videoView_layout);
        mVideoVolumeLayout = (LinearLayout) findViewById(R.id.volume_icon_layout);
        mVideoVolumeLayout.setOnClickListener(this);
        mVolumeIcon = (ImageView) findViewById(R.id.volume_icon);
        mVolumeStatusText = (TextView) findViewById(R.id.volume_status_text);
        mNoWifiFrameLayout = (FrameLayout) findViewById(R.id.video_screen_bg_layout);

    }

    private void initHotelInfoData() {
        Intent intent = getIntent();
        mHotelVideo = (String) intent.getSerializableExtra("hotelVideo");
        countVideoSize();
    }


    private void countVideoSize() throws NumberFormatException {
        if (mHotelVideo == null) {
            return;
        }
        if (TextUtils.isEmpty(mHotelVideo)) {
            return;
        }

    }

    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mCurrentPosition = mVideoView.getCurrentPosition();
            mVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        videoReadyPlay();
    }

    private void videoReadyPlay() {
        if (getNetWorkStatus()) {
            isShowHotelInfo = true;
            startVideoPlayer();
        }
    }

    private void handleErrorNetWorkStatus() {
        isShowHotelInfo = false;
        mNoWifiFrameLayout.setVisibility(View.VISIBLE);
        mVideoImageBg.setVisibility(View.VISIBLE);
        showNoWifiDialog();

    }

    private boolean getNetWorkStatus() {
        if (!HotelDetailUtils.isNetworkConnected(this)) {
            mErrorType = HotelVideoConstants.NET_NOT_AVAILABLE;
            handleErrorNetWorkStatus();
            return false;
        }
        if (!HotelDetailUtils.isWifi(this)) {
            mErrorType = HotelVideoConstants.WIFI_NOT_AVAILABLE;
            handleErrorNetWorkStatus();
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mMediaController.isShowing()) {
                mMediaController.hide();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (!mMediaController.isShowing()) {
                mMediaController.show();
            }
        }

        return true;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.e("volume", "KEYCODE_VOLUME_UP");
                refreshVolume();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.e("volume", "KEYCODE_VOLUME_DOWN");
                refreshVolume();
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                Log.e("volume", "KEYCODE_VOLUME_MUTE");
                refreshVolume();
                break;


        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mVideoView.seekTo(0);
        mHotelVideoPlayCompleted.setVisibility(View.VISIBLE);

    }

    private class VideoHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int what = msg.what;
            switch (what) {
                case HotelVideoConstants.HIDE_HOTEL_INFO:

                    mCloseLayout.setVisibility(View.GONE);
                    mVideoVolumeLayout.setVisibility(View.GONE);
                    break;
                case HotelVideoConstants.OPEN_HOTEL_INFO:

                    mCloseLayout.setVisibility(View.VISIBLE);
                    mVideoVolumeLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void startVideoPlayer() {
        mHotelVideoPlayCompleted.setVisibility(View.GONE);
        mVideoImageBg.setVisibility(View.GONE);
        mNoWifiFrameLayout.setVisibility(View.GONE);
        mNoWifiFrameLayout.setAlpha(0.0f);
        mVideoView.setVisibility(View.VISIBLE);
        mMediaController = new HotelCustomMediaController(this);
        mVideoView.setVideoURI(Uri.parse(mHotelVideo));
        mVideoView.seekTo(mCurrentPosition);
        mVideoView.setMediaController(mMediaController);
        mMediaController.setMediaPlayer(mVideoView);
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                final Message message = mHandler.obtainMessage(HotelVideoConstants.OPEN_HOTEL_INFO);
                message.sendToTarget();

            }
        });
        mVideoView.setOnCompletionListener(this);
        mMediaController.setHotelInfoLayoutVisibleCallBack(new HotelCustomMediaController.IHotelInfoLayoutVisibleCallBack() {
            @Override
            public void showHotelInfo() {

                final Message message = mHandler.obtainMessage(HotelVideoConstants.OPEN_HOTEL_INFO);
                message.sendToTarget();
            }

            @Override
            public void hideHotelInfo() {

                final Message message = mHandler.obtainMessage(HotelVideoConstants.HIDE_HOTEL_INFO);
                message.sendToTarget();
            }

        });

        mVideoLayout.setOnTouchListener(this);
        mVideoView.setOnErrorListener(this);


    }


    private void initVideoPlayerLayout() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        if (width / height > 0) { // 横屏
            initVideoLandLayout();
        }
        if (width / height == 0) { //竖屏
            initVideoPortLayout();
        }

    }


    private void initVideoPortLayout() {
        RelativeLayout.LayoutParams videoLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        videoLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mVideoView.setLayoutParams(videoLp);
        mVideoView.start();


    }

    private void initVideoLandLayout() {

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mVideoViewLayout.setLayoutParams(layoutParams);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RelativeLayout.LayoutParams closeParam = new RelativeLayout.LayoutParams(60, 60);
        closeParam.leftMargin = 30;
        closeParam.topMargin = 30;
        mCloseLayout.setLayoutParams(closeParam);

    }


    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            initVideoLandLayout();
        } else {
            initVideoPortLayout();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_close_layout) {

            if (mVideoView != null) {
                mVideoView.suspend();
            }
            finish();
        } else if (id == R.id.video_play_completed_icon) {
            startVideoPlayer();
        } else if (id == R.id.volume_icon_layout) {
            mVolumeStatus = !mVolumeStatus;
            Log.e("volume", "volumeOpenStatus" + mVolumeStatus);
            Session.getSessionInstance().putAttribute("volumeOpenStatus", mVolumeStatus);
            resetVolumeStatus();
        }
    }

    private void resetVolumeStatus() {
        showVolumeStatus(mVolumeStatus);
        if (mVolumeStatus) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, AudioManager.FLAG_PLAY_SOUND);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.suspend();
        }
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mHandler.removeMessages(HotelVideoConstants.HIDE_HOTEL_INFO);
        mHandler.removeMessages(HotelVideoConstants.OPEN_HOTEL_INFO);
    }


}

