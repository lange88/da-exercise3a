import interfaces.DA_BenOr_RMI;
import interfaces.Message;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class DA_BenOr extends UnicastRemoteObject implements DA_BenOr_RMI, Runnable {
    private int id;
    private int[] processIds;
    private int[] remoteProcessIds;
    private String remoteHost;

    DA_BenOr(int id, int[] processIds, int[] remoteProcessIds, String remoteHost) throws RemoteException {
        this.id = id;
        this.processIds = processIds;
        this.remoteProcessIds = remoteProcessIds;
        this.remoteHost = remoteHost;
    }

    private void broadCast(String message) {
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

        // connect to remote processes
        for (int processId : remoteProcessIds) {
            String name = "rmi://" + remoteHost + ":1099/DA_BenOr_" + processId;
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

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(new Random().nextInt(5000));
                broadCast("woopwoop" + id);
            } catch (InterruptedException e1) {
                System.out.println("InterruptedException occurred while running thread.");
                e1.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void receive(String m) throws RemoteException {
        System.out.println(m);
    }
}
