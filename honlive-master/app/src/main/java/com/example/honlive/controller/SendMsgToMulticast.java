package com.example.honlive.controller;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SendMsgToMulticast {
    private MulticastSocket socket;

    public SendMsgToMulticast(MulticastSocket socket) {
        this.socket = socket;
    }
    public void send(String ip, int port){
        try {
            //todo 填入 ip
            byte[] msg = new byte[]{};
            InetAddress inetAddress = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(msg, msg.length, inetAddress, port);
            socket.send(packet);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
