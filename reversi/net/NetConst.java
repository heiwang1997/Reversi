package reversi.net;

/**
 * Huang Jiahui, 2014011330, 2016/8/1 0001.
 */
final class NetConst {
    static final String BROADCAST_HEADER = "REV-MUL1.0";
    static final int BROADCAST_PORT = 4466;
    static final int SERVER_PORT = 4467;

    // This is deprecated in RFC, but still works with me :)
    static String getBroadcastName() {
        return "255.255.255.255";
    }
}
