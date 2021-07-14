import com.corundumstudio.socketio.SocketIOClient;

import java.util.Objects;

public class ConnectMessage {
    private SocketIOClient client;
    private int connectCount;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SocketIOClient getClient() {
        return client;
    }

    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectMessage that = (ConnectMessage) o;
        return connectCount == that.connectCount &&
                Objects.equals(client, that.client) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, connectCount, id);
    }
}
