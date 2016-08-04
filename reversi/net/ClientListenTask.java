package reversi.net;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
public class ClientListenTask extends Task {
    private ClientListUpdateTask clientListUpdateTask;
    private final TreeMap<String, Integer> clientList;
    private DatagramSocket udpSocket;
    private EventHandler<Event> refreshEventHandler;

    public ClientListenTask(TreeMap<String, Integer> clientList) {
        this.clientList = clientList;
    }

    @Override
    protected Object call() throws Exception {
        clientListUpdateTask = new ClientListUpdateTask(clientList, this);

        // The thread will be terminated when the task is cancelled.
        new Thread(clientListUpdateTask).start();

        udpSocket = new DatagramSocket(NetConst.BROADCAST_PORT);

        byte[] inputBuffer = new byte[65536];

        while (true) {
            DatagramPacket udpPacket = new DatagramPacket(inputBuffer, inputBuffer.length);
            udpSocket.receive(udpPacket);

            // check whether game version is the same.
            String udpData;
            try {
                String udpRawData = new String(udpPacket.getData(), 0, udpPacket.getLength());
                String udpMulVersion = udpRawData.split(" ")[0];
                if (Objects.equals(udpMulVersion, NetConst.BROADCAST_HEADER))
                    udpData = udpRawData.substring(NetConst.BROADCAST_HEADER.length() + 1);
                else
                    continue;
            } catch (Exception e) {
                continue;
            }

            if (!clientList.containsKey(udpData))
                onServerListRefreshed();

            synchronized (clientList) {
                clientList.put(udpData, 0);
            }

            if (isCancelled()) break;
        }
        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        clientListUpdateTask.cancel();
        udpSocket.close();
        return super.cancel(mayInterruptIfRunning);
    }

    public void setOnServerListRefreshed(EventHandler<Event> handler) {
        this.refreshEventHandler = handler;
    }

    void onServerListRefreshed() {
        Platform.runLater(() -> refreshEventHandler.handle(new Event(Event.ANY)));
    }
}
