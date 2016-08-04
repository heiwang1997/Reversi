package reversi.net;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import reversi.logic.AppLogic;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
class BroadcastService extends Service {

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                String packageHeader = NetConst.BROADCAST_HEADER + " " +
                        ((AppLogic.getOwnerIsBlack())? '1' : '0') + " " +
                        (String.valueOf(AppLogic.getSecsThink())) + " ";
                DatagramSocket udpSocket = new DatagramSocket();
                udpSocket.setBroadcast(true);
                while (true) {
                    // send a udp package about this server
                    // ip address is updated every time because net env might change.
                    byte[] dataToSend = (packageHeader + InetAddress.getLocalHost().getHostAddress()).getBytes();
                    DatagramPacket udpPacket = new DatagramPacket(dataToSend, dataToSend.length);
                    udpPacket.setAddress(InetAddress.getByName(NetConst.getBroadcastName()));
                    udpPacket.setPort(NetConst.BROADCAST_PORT);

                    udpSocket.send(udpPacket);
                    // every 0.5 seconds
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        if (isCancelled()) break;
                    }
                    if (isCancelled()) break;
                }
                return null;
            }
        };
    }

}
