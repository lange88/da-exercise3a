package interfaces;

public class Message {
    public enum MessageType {
        Notification, Proposal;
    }

    public MessageType type;
    public int round;
    public int value;
}
