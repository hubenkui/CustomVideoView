package com.example.videoview.customevideoview;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by bkhu on 17/4/13.
 */
public class HotelCustomMediaController extends MediaController {
    private IHotelInfoLayoutVisibleCallBack mHotelInfoCallBack;
    public HotelCustomMediaController(Context context) {
        super(context);
    }

    @Override
    public void hide() {
        super.hide();
        if (mHotelInfoCallBack != null) {
            mHotelInfoCallBack.hideHotelInfo();
        }


    }

    @Override
    public void show() {
        super.show();
        if (mHotelInfoCallBack != null) {
            mHotelInfoCallBack.showHotelInfo();
        }
    }

    interface IHotelInfoLayoutVisibleCallBack {
        void showHotelInfo();

        void hideHotelInfo();

    }


    public void setHotelInfoLayoutVisibleCallBack(IHotelInfoLayoutVisibleCallBack visibleCallBack) {
        mHotelInfoCallBack = visibleCallBack;
    }
}
