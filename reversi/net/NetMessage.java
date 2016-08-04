package reversi.net;

/**
 * Huang Jiahui, 2014011330, 2016/8/2 0002.
 */
public class NetMessage {
    public NetEvent event;
    private String content;

    private NetMessage() {

    }

    public NetEvent getEvent() {
        return event;
    }

    public void setEvent(NetEvent event) {
        this.event = event;
    }

    public String getContent() {
        return content;
    }

    private void setContent(String content) {
        this.content = content;
    }

    public NetMessage(NetEvent event, String content) {
        this.event = event;
        this.content = content;
    }

    static NetMessage fromString(String rawMessage) {
        NetMessage result = new NetMessage();
        result.setContent(rawMessage.substring(1));
        result.setEvent(NetEvent.fromChar(rawMessage.charAt(0)));
        return result;
    }

    public String getString() {
        return NetEvent.toChar(event) + content;
    }
}
