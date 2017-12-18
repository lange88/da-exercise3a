package interfaces;

public class Message {
    public final Type type;
    private final int round;
    public final int value;

    public Message(Type type, int round, int value){
        this.type = type;
        this.round = round;
        this.value = value;
    }

    public enum Type {NOTIFICATION, PROPOSAL}
}