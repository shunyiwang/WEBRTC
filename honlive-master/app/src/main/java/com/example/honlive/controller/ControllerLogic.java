package com.example.honlive.controller;

import java.io.IOException;
import java.net.MulticastSocket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ControllerLogic implements ControllerInterface {
    private MulticastSocket joinSocket;
    private MulticastSocket sendSocket;
    private JoinMulticast joinMulticast;
    private String ip;
    private int port;
    private SendMsgToMulticast sendMsgToMulticast;
    private Queue<UrlMessage> queue;
    private boolean isAnchor;
    public ControllerLogic(String ip, int port, boolean isAnchor){
        this.ip = ip;
        this.port = port;
        this.isAnchor = isAnchor;
        queue = new LinkedBlockingQueue();
        try {
            joinSocket = new MulticastSocket();
            joinMulticast = new JoinMulticast(joinSocket, this);
            sendSocket = new MulticastSocket();
            sendMsgToMulticast = new SendMsgToMulticast(sendSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void join() {
        joinMulticast.join(ip, port);
    }

    @Override
    public void send() {
        sendMsgToMulticast.send(ip, port);
    }

    @Override
    public String recv(String msg) {
        String[] rec = msg.split(",");
        String state = rec[0];
        String url = rec[1];
        if (isAnchor) {
            if (queue.isEmpty() && state.equals("in")) {
                UrlMessage message = new UrlMessage();
                message.setUrl(url);
                message.setNum(0);
                queue.offer(message);
            }else if (!queue.isEmpty() && state.equals("in")) {
                UrlMessage message = queue.peek();
                if (message.getNum() == 0){
                    //todo 建立队首与当前url的管道，不出队
                }else if (message.getNum() == 1) {
                    //todo 建立队首与当前url的管道,并出队列
                }
            }
        }
        return null;
    }
}
