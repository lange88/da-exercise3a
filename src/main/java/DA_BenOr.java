import interfaces.DA_BenOr_RMI;
import interfaces.Message;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class DA_BenOr extends UnicastRemoteObject implements DA_BenOr_RMI, Runnable {
    private int id;
    private int[] processIds;
    private int[] remoteProcessIds;
    private String remoteHost;
    private final Object msgLock = new Object();
    private int totalNodes;
    private int maliciousNodes;
    private boolean isMalicious;

    private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    private int ultimateChosenValue = -1;
    private int value = new Random().nextInt(2);
    HashMap<Integer, Integer> randomValues = new HashMap<>();
    private boolean decided = false;

    DA_BenOr(int id, int[] processIds, int fractionMalicious, boolean malicious) throws RemoteException {
        this.id = id;
        this.processIds = processIds;
        this.totalNodes = processIds.length;
        this.maliciousNodes = totalNodes / fractionMalicious;
        this.isMalicious = malicious;
    }

    @Override
    public void receive(Message m) throws RemoteException {
            messages.add(m);
        if (!decided) System.out.println("[" + id + "] received message " + m);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000 + new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int round = 1;
        decided = false;

        while(true) {
            if (!decided) System.out.println("[" + id + "] entering notification phase (" + round + ") value: " + value + " malicious=" + isMalicious);
            try {
                // notification phase
                messages = Collections.synchronizedList(new ArrayList<Message>());
                Thread.sleep(new Random().nextInt(2000)); // random delay before broadcasting

                // random failure pattern
                if (isMalicious) {
                // randomly decide to send or not
                    if (new Random().nextFloat() < 0.5) {
                        // decide to send correct value of random one
                        if (new Random().nextFloat() < 0.5) {
                            broadcast(new Message(Message.Type.NOTIFICATION, round, value));
                        } else {
                            broadcast(new Message(Message.Type.NOTIFICATION, round, new Random().nextInt(2)));
                        }
                    }
                } else {
                    broadcast(new Message(Message.Type.NOTIFICATION, round, value));
                }

                //broadcast(new Message(Message.Type.NOTIFICATION, round, value));

                /*Awaiting messages*/
                while (countMessagesOfType(Message.Type.NOTIFICATION) < (totalNodes - maliciousNodes)) {
                    Thread.sleep(100);
                }
                if (!decided) System.out.println("[" + id + "] entering proposal phase (" + round + ")");
                // proposal phase
                int notificationValue = findNotificationValue();
                Thread.sleep(new Random().nextInt(2000)); // random delay before broadcasting

                // random failure pattern
                if (isMalicious) {
                    // randomly decide to send or not
                    if (new Random().nextFloat() < (0.5)) {
                        // decide to send correct value of random one
                        if (new Random().nextFloat() < (0.5)) {
                            broadcast(new Message(Message.Type.NOTIFICATION, round, notificationValue));
                        } else {
                            broadcast(new Message(Message.Type.NOTIFICATION, round, new Random().nextInt(2)));
                        }
                    }
                } else {
                    broadcast(new Message(Message.Type.NOTIFICATION, round, notificationValue));
                }
                //broadcast(new Message(Message.Type.PROPOSAL, round, notificationValue));

                if (decided) {
                    break;
                }
                else {
                    //messages = new ArrayList<>();
                    while (countMessagesOfType(Message.Type.PROPOSAL) < (totalNodes - maliciousNodes)) {
                        Thread.sleep(100);
                    }
                }
                System.out.println("[" + id + "] entering decision phase (" + round + ")");
                // decision phase
                int proposalValue = findProposalValue();
                if (proposalValue != -1) {
                    value = proposalValue;
                    if (countMessagesOfTypeAndValue(Message.Type.PROPOSAL, value) > ((totalNodes + maliciousNodes)/2)){
                        ultimateChosenValue = value;
                        decided = true;
                        System.out.println("[" + id + "] decided on " + ultimateChosenValue);
                        printRandomValues();
                    }
                } else {
                    Random random = new Random();
                    value = random.nextInt(2);
                    randomValues.put(round, value);
                }
                round++;
            } catch (InterruptedException e1) {
                System.out.println("InterruptedException occurred while running thread.");
                e1.printStackTrace();
                return;
            }
        }
    }

    private void printRandomValues() {
        System.out.print("[" + id + "] random values used: ");
        for (Integer i : randomValues.keySet()) {
            System.out.print(i + ":" + randomValues.get(i) + " ");
        }
        System.out.print("\n");
    }

    private void broadcast(Message message) {
        // connect to local processes
        for (int processId : processIds) {
            String name = "rmi://localhost:1099/DA_BenOr_" + processId;
            try {
                DA_BenOr_RMI o = (DA_BenOr_RMI) java.rmi.Naming.lookup(name);
                o.receive(message);
            } catch (NotBoundException e1) {
                System.out.println("NotBoundException while sending message for name: " + name);
                e1.printStackTrace();
            } catch (MalformedURLException e1) {
                System.out.println("MalformedURLException while sending message for name: " + name);
                e1.printStackTrace();
            } catch (RemoteException e1) {
                System.out.println("RemoteException while sending message for name: " + name);
                e1.printStackTrace();
            }
        }
    }

    private long countMessagesOfType(Message.Type requiredType){
        long ret = 0;
            ret = messages.stream()
                    .filter(m -> m.type == requiredType)
                    .count();
        return ret;
    }

    private int findNotificationValue(){
        int zeroCounter = 0;
        int oneCounter = 0;
            for (Message msg : messages) {
                switch (msg.value) {
                    case 0:
                        zeroCounter++;
                        break;
                    case 1:
                        oneCounter++;
                        break;
                    default:
                        break;
                }
            }
        if (oneCounter  > (totalNodes + maliciousNodes)/2){
            return 1;
        }
        if (zeroCounter > (totalNodes + maliciousNodes)/2) {
            return 0;
        }
        else {
            return -1;
        }
    }

    private int findProposalValue() {
        int zeroCounter = 0;
        int oneCounter = 0;
            for (Message msg : messages) {
                switch (msg.value) {
                    case 0:
                        zeroCounter++;
                        break;
                    case 1:
                        oneCounter++;
                        break;
                    default:
                        break;
                }
            }
        if (oneCounter  > zeroCounter && oneCounter > maliciousNodes){
            return 1;
        }
        if (zeroCounter > oneCounter && zeroCounter > maliciousNodes) {
            return 0;
        }
        else {
            Random random = new Random();
            return random.nextInt(2);
        }
    }

    private long countMessagesOfTypeAndValue(Message.Type requiredType, int requiredValue){
        long ret = 0;
            ret = messages.stream()
                    .filter(m -> m.type == requiredType)
                    .filter(m -> m.value == requiredValue)
                    .count();

        return ret;
    }
}
