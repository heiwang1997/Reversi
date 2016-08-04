package reversi.net;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Huang Jiahui, 2014011330, 2016/8/2 0002.
 *
 * Received a message once a time. Then the task is restarted until being cancelled.
 */
public class ReceiveMessageService extends ScheduledService<NetMessage> {

    private final Socket socket;

    public ReceiveMessageService(Socket socket) {
        this.socket = socket;
        setRestartOnFailure(false);
    }

    @Override
    protected Task<NetMessage> createTask() {
        return new Task<NetMessage>() {
            @Override
            protected NetMessage call() throws Exception {
                // socket close auto detect
                BufferedReader bufferedReaderIn = new
                        BufferedReader(new InputStreamReader(socket.getInputStream()));
                String incomingMessage = bufferedReaderIn.readLine();
                return NetMessage.fromString(incomingMessage);
            }
        };
    }

}
