package com.example.honlive;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class SelfMessageActivity extends AppCompatActivity {
    ImageView h_Back;
    ImageView h_Head;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_message);
        //实现个人中心头部磨砂布局

        h_Back = findViewById(R.id.h_back);
        h_Head = findViewById(R.id.h_head);
        Glide.with(this).load(R.mipmap.head_new)
                .bitmapTransform(new BlurTransformation(this, 25), new CenterCrop(this))
                .into(h_Back);

        //设置圆形图像
        Glide.with(this).load(R.mipmap.head_new)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(h_Head);

    }
}
