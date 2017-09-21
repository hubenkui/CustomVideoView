package com.example.videoview.customevideoview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    Button mPlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayBtn = (Button) findViewById(R.id.player_btn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HotelVideoActivity.class);
                intent.putExtra("hotelVideo","https://video.c-ctrip.com/videos/u00b0c00000065uyyBF17.mp4");
                startActivity(intent);
            }
        });


    }


}
