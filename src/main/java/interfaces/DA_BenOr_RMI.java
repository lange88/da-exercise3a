package interfaces;

import java.rmi.Remote;

public interface DA_BenOr_RMI extends Remote {
    void receive(String m) throws java.rmi.RemoteException;
}
