package com.example.videoview.customevideoview;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by bkhu on 17/4/11.
 */
public class HotelVideoNoWifiFragment extends Fragment implements View.OnClickListener {

    private TextView mContinuePlay;
    private TextView mContinuePlayTip;
    private String mErrorType;
    private String mVideoSize;


    public static HotelVideoNoWifiFragment newInstance(String errorType, String videoSize) {
        HotelVideoNoWifiFragment videoNoWifiFragment = new HotelVideoNoWifiFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HotelVideoConstants.NET_ERROR_TYPE, errorType);
        bundle.putString(HotelVideoConstants.VIDEO_SIZE, videoSize);
        videoNoWifiFragment.setArguments(bundle);
        return videoNoWifiFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mErrorType = bundle.getString(HotelVideoConstants.NET_ERROR_TYPE);
            mVideoSize = bundle.getString(HotelVideoConstants.VIDEO_SIZE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View contentView = LayoutInflater.from(this.getActivity()).inflate(R.layout.hotel_video_no_wifi_tip_layout, null);
        mContinuePlay = (TextView) contentView.findViewById(R.id.continue_play);
        mContinuePlayTip = (TextView) contentView.findViewById(R.id.continue_play_tip);
        if (HotelVideoConstants.NET_NOT_AVAILABLE.equals(mErrorType)) {
            mContinuePlayTip.setText("网络未连接，请检查网络设置");
            mContinuePlay.setText("刷新重试");
        } else if (HotelVideoConstants.WIFI_NOT_AVAILABLE.equals(mErrorType)) {
            SpannableStringBuilder notWiFiNotice = new SpannableStringBuilder();
            notWiFiNotice.append("您正处于非WiFi网络环境观看视频会消耗流量");
            if (null != mVideoSize) {
                notWiFiNotice.append(mVideoSize);
            }

            if (isTooLongForShow(notWiFiNotice.toString(), mContinuePlayTip)) {
                notWiFiNotice.insert(notWiFiNotice.toString().indexOf("，") + 1, "\n");//如果超过一行，从逗号后开始折行展示
            }
            notWiFiNotice.append("，是否继续播放");
            mContinuePlayTip.setText(notWiFiNotice);
            mContinuePlay.setText("继续播放");
        }
        mContinuePlay.setOnClickListener(this);
        return contentView;
    }

    private Boolean isTooLongForShow(String content, TextView tv) {
        if (tv == null || content == null) {
            return false;
        }

        int displayWidth =  600;//layout_width="315dp"

        Rect bounds = new Rect();
        tv.getPaint().getTextBounds(content, 0, content.length(), bounds);
        int width = bounds.width();

        if (width > displayWidth) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.continue_play) {
            DisMissFragment();
        }

    }


    public void setStartPlayVideoCallBack(IStartPlayVideoCallBack refreshShareStatusCallBack) {
        this.mStartPlayVideoCallBack = refreshShareStatusCallBack;
    }

    public IStartPlayVideoCallBack mStartPlayVideoCallBack;

    public interface IStartPlayVideoCallBack {
        void startPlayVideo(String netType);
    }

    public void DisMissFragment() {

        if (HotelVideoNoWifiFragment.this.getFragmentManager() != null) {
            HotelVideoNoWifiFragment.this.getFragmentManager().popBackStack();
        }
        if (mStartPlayVideoCallBack != null) {
            mStartPlayVideoCallBack.startPlayVideo(mErrorType);
        }
    }
}
