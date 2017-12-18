class Message {

    final Type type;
    private final int round;
    final int value;

    Message(Type type, int round, int value){
        this.type = type;
        this.round = round;
        this.value = value;
    }

    enum Type {NOTIFICATION, PROPOSAL}
}