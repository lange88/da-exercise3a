package interfaces;

import java.rmi.Remote;

public interface DA_BenOr_RMI extends Remote {
    void receive(Message m) throws java.rmi.RemoteException;
}
