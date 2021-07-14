package com.example.honlive.controller;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class JoinMulticast {
    private MulticastSocket socket;
    private ControllerLogic logic;
    public JoinMulticast(MulticastSocket socket, ControllerLogic logic) {
        this.socket = socket;
        this.logic = logic;
    }

    public void join(final String ip, final int port){
      new Thread(new Runnable() {
          @Override
          public void run() {
              try {
                  socket = new MulticastSocket(port);
                  InetAddress inetAddress = InetAddress.getByName(ip);
                  socket.joinGroup(inetAddress);
                  while (true) {
                      byte[] data = new byte[100];
                      DatagramPacket packet = new DatagramPacket(data, data.length);
                      socket.receive(packet);
                      String msg = new String(data);
                      if (!msg.equals("")){
                          String finMsg = msg.split("#")[0];
                          //todo 回调
                          logic.recv(finMsg);
                      }
                  }
              }catch (Exception e) {
                  e.printStackTrace();
              }
          }
      }).start();
   }
}
