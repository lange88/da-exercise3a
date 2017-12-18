package interfaces;

import java.io.Serializable;

public class Message implements Serializable {
    public final Type type;
    private final int round;
    public final int value;
    private static final long serialVersionUID = 20120731125400L;

    public Message(Type type, int round, int value){
        this.type = type;
        this.round = round;
        this.value = value;
    }

    public enum Type {NOTIFICATION, PROPOSAL}

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", round=" + round +
                ", value=" + value +
                '}';
    }
}