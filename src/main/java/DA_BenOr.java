import interfaces.DA_BenOr_RMI;
import interfaces.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DA_BenOr extends UnicastRemoteObject implements DA_BenOr_RMI, Runnable {
    private int id;
    private int[] processes;

    DA_BenOr(int id, int[] processes) throws RemoteException {
        this.id = id;
        this.processes = processes;
    }
    @Override
    public void run() {

    }

    @Override
    public void receive(Message m) throws RemoteException {

    }
}
