package com.example.honlive;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class MyTextView extends AppCompatTextView {
    public MyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    public MyTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    @Override
    public boolean isFocused() {
        // TODO Auto-generated method stub
        return true;
    }
}
