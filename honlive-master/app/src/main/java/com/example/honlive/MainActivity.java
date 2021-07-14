package com.example.honlive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.honlive.client.PipleMessage;
import com.example.honlive.client.SignalingClient;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;


public class MainActivity extends AppCompatActivity  {
    private int imageIds[] = {
            R.mipmap.pic1, R.mipmap.pic2,
            R.mipmap.pic3, R.mipmap.pic4,
            R.mipmap.pic5
    };
    private ArrayList<ImageView> images = new ArrayList<>();
    private ViewPager vp;
    private int oldPosition = 0;//记录上一次点的位置
    private int currentItem; //当前页面
    private ScheduledExecutorService scheduledExecutorService;

    //图片标题
    //private String titles[] = new String[]{"图片1", "图片2", "图片3", "图片4", "图片5"};
    private ArrayList<View> dots = new ArrayList<View>();
    private TextView title;
    private Button color;


    NavigationView navigationView;
    View view_layout;
    MainActivity  mainAc = this;


    String ip;
    int port;
    private Context context;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch(item.getItemId()){
            case R.id.navigation_item_one:
                Intent intent1 =new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent1);
                break;
            case R.id.navigation_item_two:
                Intent intent2 =new Intent(MainActivity.this,ConnectServerActivity.class);
                startActivity(intent2);
                break;
            case R.id.navigation_item_three  :
                Intent intent3 =new Intent(MainActivity.this,SelfMessageActivity.class);
                startActivity(intent3);
                break;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        Slide();//实现手动滑动功能的函数
        onStart();//实现自动轮播功能的函数

        // 抽屉menu item事件
        navigationView  = findViewById(R.id.drawer_navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.navigation_item_one:
                        Intent intent1 =new Intent(MainActivity.this,MainActivity.class);
                        startActivity(intent1);
                        MainActivity.this.finish();
                        break;
                    case R.id.navigation_item_two:
                        Intent intent2 =new Intent(MainActivity.this,ConnectServerActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.navigation_item_three  :
                        Intent intent3 =new Intent(MainActivity.this,SelfMessageActivity.class);
                        startActivity(intent3);
                        break;
                }
                return true;
            }
        });
        context = this;
        Button buttonLive = (Button) findViewById(R.id.button_live);
        Button buttonWatch = (Button) findViewById(R.id.button_watch);

        buttonLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent starter = new Intent(context, LiveActivity.class);
                startActivity(starter);
                finish();
            }
        });
        buttonWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent starter = new Intent(context, WatchActivity.class);
                startActivity(starter);
            }
        });
    }
    public void Slide(){
        for (int i = 0; i < imageIds.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(imageIds[i]);
            images.add(imageView);
        }

        //显示的点 加入集合
        dots.add(findViewById(R.id.dot_0));
        dots.add(findViewById(R.id.dot_1));
        dots.add(findViewById(R.id.dot_2));
        dots.add(findViewById(R.id.dot_3));
        dots.add(findViewById(R.id.dot_4));
        //获取图片标题的id
        title = (TextView) findViewById(R.id.tv_test_title);
        //获取ViewPager 的id
        vp = (ViewPager) findViewById(R.id.vp);
        vp.setAdapter(new ViewPagerAdapter());
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                //设置页面刷新后的图片标题
                // title.setText(titles[position]);

                oldPosition = position;
                currentItem = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //将试图移除试图组
            View v =images.get(position);
            container.removeView(v);
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //将试图添加进试图组
            View v =images.get(position);
            container.addView(v);
            return v;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //每隔三秒换一张图片
        scheduledExecutorService.scheduleWithFixedDelay(
                new ViewPageTask(),
                3,
                3,
                TimeUnit.SECONDS
        );
    }
    //实现一个碎片的接口
    private class ViewPageTask implements Runnable{

        @Override
        public void run() {
            currentItem = (currentItem + 1) % imageIds.length;
            mHandler.sendEmptyMessage(0);
        }
    }
    /**
     * 接收子线程传递过来的数据
     */
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            vp.setCurrentItem(currentItem);
        };
    };
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if(scheduledExecutorService != null){
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }
}