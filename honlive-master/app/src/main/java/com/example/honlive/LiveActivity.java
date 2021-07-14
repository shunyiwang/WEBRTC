package com.example.honlive;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.honlive.client.PeerConnectionAdapter;
import com.example.honlive.client.SdpAdapter;
import com.example.honlive.client.SignalingClient;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStats;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LiveActivity extends AppCompatActivity implements SignalingClient.Callback {

    private ConcurrentHashMap<String, PeerConnection> peerConnectionMap;
    private List<PeerConnection.IceServer> iceServers;
    private EglBase.Context eglBaseContext;
    private PeerConnectionFactory peerConnectionFactory;
    private SurfaceViewRenderer localView;
    private MediaStream mediaStream;
    private SurfaceViewRenderer[] remoteViews;
    private Context context;
    private SurfaceTextureHelper surfaceTextureHelper;
    boolean isAudience = false;
    MediaStream liveStream = null;
    RTCStats preRtcStats = null;
    private boolean isFront = false;
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        context = this;
        peerConnectionMap = new ConcurrentHashMap<>();
        iceServers = new ArrayList<>();
        eglBaseContext = EglBase.create().getEglBaseContext();
        //读文件
        String url = getDatafromFile(context.getFilesDir().getPath().toString()+"/address.txt");
        System.out.println("url-----" + url);
        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory =
                new DefaultVideoEncoderFactory(eglBaseContext, true, true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory =
                new DefaultVideoDecoderFactory(eglBaseContext);
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);

        // create VideoCapturer


        mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream");
        setCamera(isFront);
        new Thread(()->{
            SignalingClient.get().init(this, url);
        }).start();
    }
    private void setCamera(boolean front){
        videoCapturer = createCameraCapturer(front);
         videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());

        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(1920, 1080, 30);
        localView = findViewById(R.id.localView);
        localView.setMirror(false);
        localView.init(eglBaseContext, null);
        // create VideoTrack
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        // display in localView
        videoTrack.addSink(localView);

        // create audioTrack
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        mediaStream.addTrack(videoTrack);
        mediaStream.addTrack(audioTrack);

    }

    private VideoCapturer createCameraCapturer(boolean isFront) {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private synchronized PeerConnection getOrCreatePeerConnection(String socketId) {
        PeerConnection peerConnection = peerConnectionMap.get(socketId);
        if (peerConnection != null) {
            return peerConnection;
        }
        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter("PC:" + socketId) {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                SignalingClient.get().sendIceCandidate(iceCandidate, socketId);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
//                runOnUiThread(() -> {
//                    Toast.makeText(context, "显示远程视频", Toast.LENGTH_LONG).show();
//                    remoteVideoTrack.addSink(localView);
//                });
                liveStream = mediaStream;
            }
        });
        peerConnection.setBitrate(2000000, 2000000, 2000000);

//        if (isLive) {
        if (isAudience) {
            if (liveStream != null) {
                runOnUiThread(() -> {
                    Toast.makeText(context, "转发远程视频", Toast.LENGTH_LONG).show();
                });
                peerConnection.addStream(liveStream);
            }
        } else {
            peerConnection.addStream(mediaStream);
        }
//        }

        peerConnectionMap.put(socketId, peerConnection);
        return peerConnection;
    }

    @Override
    public void onCreateRoom() {

    }

    @Override
    public void onPeerJoined(String socketId) {
        PeerConnection peerConnection = getOrCreatePeerConnection(socketId);
        peerConnection.createOffer(new SdpAdapter("createOfferSdp:" + socketId) {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new SdpAdapter("setLocalSdp:" + socketId), sessionDescription);
                SignalingClient.get().sendSessionDescription(sessionDescription, socketId);

            }
        }, new MediaConstraints());
    }

    @Override
    public void onSelfJoined() {

    }

    @Override
    public void onPeerLeave(String msg) {

    }

    @Override
    public void onOfferReceived(JSONObject data) {
        isAudience = true;
        runOnUiThread(() -> {
            final String socketId = data.optString("from");
            PeerConnection peerConnection = getOrCreatePeerConnection(socketId);
            peerConnection.setRemoteDescription(new SdpAdapter("setRemoteSdp:" + socketId),
                    new SessionDescription(SessionDescription.Type.OFFER, data.optString("sdp")));
            Log.i("sdp", data.optString("sdp"));
            peerConnection.createAnswer(new SdpAdapter("localAnswerSdp") {
                @Override
                public void onCreateSuccess(SessionDescription sdp) {
                    super.onCreateSuccess(sdp);
                    peerConnectionMap.get(socketId).setLocalDescription(new SdpAdapter("setLocalSdp:" + socketId), sdp);
                    SignalingClient.get().sendSessionDescription(sdp, socketId);
                }
            }, new MediaConstraints());

        });
    }

    @Override
    public void onAnswerReceived(JSONObject data) {
        String socketId = data.optString("from");
        PeerConnection peerConnection = getOrCreatePeerConnection(socketId);
        peerConnection.setRemoteDescription(new SdpAdapter("setRemoteSdp:" + socketId),
                new SessionDescription(SessionDescription.Type.ANSWER, data.optString("sdp")));
        Log.i("sdp", data.optString("sdp"));
    }

    @Override
    public void onIceCandidateReceived(JSONObject data) {
        String socketId = data.optString("from");
        PeerConnection peerConnection = getOrCreatePeerConnection(socketId);
        peerConnection.addIceCandidate(new IceCandidate(
                data.optString("id"),
                data.optInt("label"),
                data.optString("candidate")
        ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SignalingClient.get().destroy();
    }
    //读文件
    public String getDatafromFile(String fileName) {

        String Path=fileName;
        BufferedReader reader = null;
        String laststr = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr;
    }
}
