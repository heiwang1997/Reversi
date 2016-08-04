package reversi.net;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Huang Jiahui, 2014011330, 2016/8/2 0002.
 */
public class SendMessageService extends ScheduledService {
    private final LinkedList<String> queuedMessage;
    private final Socket socket;

    public SendMessageService(Socket socket) {
        this.socket = socket;
        queuedMessage = new LinkedList<>();
        setRestartOnFailure(false);
    }

    public void sendMessage(NetMessage message) {
        synchronized (queuedMessage) {
            queuedMessage.addLast(message.getString());
            queuedMessage.notify();
        }
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                String messageToSend;
                synchronized (queuedMessage) {
                    if (queuedMessage.size() == 0) {
                        try { queuedMessage.wait(); }
                        catch (InterruptedException ignored) {}
                    }
                    // assert: qM.size != 0
                    messageToSend = queuedMessage.removeFirst();
                }
                // when exception was thrown, the service fails (indicates disconnection)
                DataOutputStream dataStreamOut = new DataOutputStream(socket.getOutputStream());
                dataStreamOut.writeBytes(messageToSend + "\n");
                return null;
            }
        };
    }
}
