import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class byzantine {

    private static final int totalNodes = 10;
    private static final int maliciousNodes = totalNodes/5;


    private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    private int ultimateChosenValue = -1;
    private int value = -1;

    public void byzantine(){
        int round = 1;
        boolean decided = false;

        while (true){
            // notification phase
            broadcast(new Message(Message.Type.NOTIFICATION, round, value));

            /*Awaiting messages*/
            while (countMessagesOfType(Message.Type.NOTIFICATION) < (totalNodes - maliciousNodes)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // proposal phase
            int notificationValue = findNotificationValue();
            broadcast(new Message(Message.Type.PROPOSAL, round, notificationValue));

            if (decided) {
                break;
            }
            else {
                messages = new ArrayList<>();
                while (countMessagesOfType(Message.Type.PROPOSAL) < (totalNodes - maliciousNodes)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // decision phase
            int proposalValue = findProposalValue();
            if (proposalValue != -1) {
                value = proposalValue;
                if (countMessagesOfTypeAndValue(Message.Type.PROPOSAL, value) > (3 * maliciousNodes)){
                    ultimateChosenValue = value;
                    decided = true;
                }
            } else {
                Random random = new Random();
                value = random.nextInt(2);
            }
            round++;
        }

    }

    private void broadcast(Message message){
        // broadcast
    }

    private long countMessagesOfType(Message.Type requiredType){
        return messages.stream()
                .filter(m -> m.type == requiredType)
                .count();
    }

    private int findNotificationValue(){
        int zeroCounter = 0;
        int oneCounter = 0;
        for (Message msg : messages){
            switch (msg.value) {
                case 0: zeroCounter++; break;
                case 1: oneCounter++; break;
                default: break;
            }
        }
        if (zeroCounter > (totalNodes + maliciousNodes)/2) {
            return 0;
        }
        if (oneCounter  > (totalNodes + maliciousNodes)/2){
            return 1;
        }
        else {
            return -1;
        }
    }

    private int findProposalValue() {
        int zeroCounter = 0;
        int oneCounter = 0;
        for (Message msg : messages){
            switch (msg.value) {
                case 0: zeroCounter++; break;
                case 1: oneCounter++; break;
                default: break;
            }
        }
        if (zeroCounter > maliciousNodes) {
            return 0;
        }
        if (oneCounter  > maliciousNodes){
            return 1;
        }
        else {
            return -1;
        }
    }

    private long countMessagesOfTypeAndValue(Message.Type requiredType, int requiredValue){
        return messages.stream()
                .filter(m -> m.type == requiredType)
                .filter(m -> m.value == requiredValue)
                .count();
    }
}
