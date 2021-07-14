import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static List<ConnectMessage> connectMessageList = new ArrayList<>();
    private static Map<String, SocketIOClient> socketIOClientMap = new ConcurrentHashMap<>();
    private static Map<String, List<String>> fromToTo = new ConcurrentHashMap<>();
    private static Map<String, String> toToFrom = new ConcurrentHashMap<>();
    private static String room;
    private static volatile SocketIOServer server;
    private volatile static Server instance = null;
    private Server(){}
    public static Server getInstance(){
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null){
                    instance = new Server();
                }
            }
        }
        return instance;
    }
    public void startServer(int port){
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(port);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        server = new SocketIOServer(config);
    }
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        getInstance().startServer(port);
        // 添加客户端连接监听器
        server.addConnectListener(client -> {
            System.out.println(client.getRemoteAddress() + " web客户端接入");
            String[] msg = {"Message from server:"};
            ConnectMessage connectMessage = new ConnectMessage();
            connectMessage.setClient(client);
            connectMessage.setConnectCount(0);
            connectMessage.setId(client.getSessionId().toString());
            connectMessageList.add(connectMessage);
            socketIOClientMap.put(client.getSessionId().toString(), client);
            client.sendEvent("log", msg);

        });

        server.addEventListener("message", Message.class, new DataListener<Message>() {
            @Override
            public void onData(SocketIOClient socketIOClient, Message message, AckRequest ackRequest) throws Exception {
                System.out.println("message-----------" + message);
//                server.getBroadcastOperations().sendEvent("message", message);
                socketIOClientMap.get(message.getTo()).sendEvent("message", message);
            }
        });
        server.addEventListener("create or join", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception {
                if (server.getRoomOperations(s).getClients() == null ||
                        server.getRoomOperations(s).getClients().isEmpty()) {
                    room = s;
                    socketIOClient.joinRoom(s);
                    socketIOClient.sendEvent("created", s, socketIOClient.getSessionId());
                }else {
                    for (ConnectMessage message : connectMessageList) {
                        System.out.println("connectList---------"  + message.getClient() + "::" + message.getId() + "::" + message.getConnectCount());
                        if (message.getConnectCount() == 0 || message.getConnectCount() == 1) {
                            message.getClient().sendEvent("join", s, socketIOClient.getSessionId());
                            String from = message.getClient().getSessionId().toString();
                            String to = socketIOClient.getSessionId().toString();
                            List<String> tos = fromToTo.get(from);
                            if (tos == null) {
                                tos = new ArrayList<>();
                                tos.add(to);
                                fromToTo.put(from, tos);
                            }else {
                                tos.add(to);
                            }
                            toToFrom.put(to, from);
                            message.setConnectCount(message.getConnectCount() + 1);
                            break;
                        }
                    }

                    socketIOClient.joinRoom(s);
                    socketIOClient.sendEvent("joined", s, socketIOClient.getSessionId());
//                    server.getBroadcastOperations().sendEvent("ready");

                }
            }
        });
        server.addEventListener("bye", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception {
                //todo cough have bug
                String from = toToFrom.get(s);
                List<String> tos = fromToTo.get(s);
                List<SocketIOClient> socketIOClients = new ArrayList<>();

                for (int i = 0; i < connectMessageList.size(); i++) {
                    if (connectMessageList.get(i).getId().equals(from)) {
                        connectMessageList.get(i).setConnectCount(connectMessageList.get(i).getConnectCount() - 1);
                    }
                    if (connectMessageList.get(i).getId().equals(s)) {
                        connectMessageList.remove(connectMessageList.get(i));
                    }
                    if (tos != null && tos.size() !=0 && tos.contains(connectMessageList.get(i).getId())) {
                        connectMessageList.remove(connectMessageList.get(i));
                    }
                }

                socketIOClientMap.remove(socketIOClient.getSessionId().toString());

                if (tos != null) {
                    for (String id : tos) {
                        socketIOClients.add(socketIOClient.get(id));
                    }
                    for (SocketIOClient client : socketIOClients) {
                        System.out.println("room-------" + room + client);
                        if (client != null) {
                            client.leaveRoom(room);
                            client.sendEvent("bye", client.getSessionId().toString());
                        }
                    }

                }

                if (fromToTo.get(s) != null) {
                    fromToTo.remove(s);
                }
                if (toToFrom.get(s) != null && fromToTo.get(toToFrom.get(s)) != null) {
                    fromToTo.get(toToFrom.get(s)).remove(s);
                    toToFrom.remove(s);
                }
            }
        });
        server.start();
    }
}
