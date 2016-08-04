package reversi.net;

import javafx.concurrent.Task;

import java.net.Socket;

/**
 * Huang Jiahui, 2014011330, 2016/8/2 0002.
 */
public class ClientConnectionTask extends Task<Socket> {
    // default: connect to localhost
    private String hostName = "localhost";

    public ClientConnectionTask(String hostName) {
        this.hostName = hostName;
    }

    @Override
    protected Socket call() throws Exception {
        return new Socket(hostName, NetConst.SERVER_PORT);
    }
}
