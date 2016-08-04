package reversi.net;

import javafx.concurrent.Task;

import java.util.Map;
import java.util.TreeMap;

/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
class ClientListUpdateTask extends Task {
    private final TreeMap<String, Integer> clientList;
    private final ClientListenTask parent;

    ClientListUpdateTask(TreeMap<String, Integer> clientList, ClientListenTask parent) {
        this.clientList = clientList;
        this.parent = parent;
    }

    @Override
    protected Object call() throws Exception {
        while (true) {
            if (isCancelled()) return null;
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                if (isCancelled()) return null;
            }
            synchronized (clientList) {
                for (Map.Entry<String, Integer> entry : clientList.entrySet()) {
                    entry.setValue(entry.getValue() + 200);
                    if (entry.getValue() > 1200) {
                        clientList.remove(entry.getKey());
                        parent.onServerListRefreshed();
                    }
                }
            }
        }
    }
}
