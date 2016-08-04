package reversi.net;

/**
 * Huang Jiahui, 2014011330, 2016/8/2 0002.
 */
public enum NetEvent {
    CHESS, TIC, WITHDRAW, NEW, MESSAGE, NIL;

    public static char toChar(NetEvent event) {
        switch (event) {
            case CHESS:
                return 0x00;
            case TIC:
                return 0x01;
            case WITHDRAW:
                return 0x02;
            case NEW:
                return 0x03;
            case MESSAGE:
                return 0x04;
            default:
                return 0xFF;
        }
    }

    public static NetEvent fromChar(char c) {
        switch (c) {
            case 0x00:
                return CHESS;
            case 0x01:
                return TIC;
            case 0x02:
                return WITHDRAW;
            case 0x03:
                return NEW;
            case 0x04:
                return MESSAGE;
            default:
                return NIL;
        }
    }
}
