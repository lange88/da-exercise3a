import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * Created by jeroen on 12/8/17.
 */
public class DA_BenOr_main {
    public static void main(String... args) {
        int processIds[] = {1, 2, 3, 4, 5};
        int remoteProcessIds[] = {6, 7, 8, 9, 10};
        String remoteHost = "10.0.0.2";

        // Create and install a security manager because we are using multiple
        // physical machines.
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // create local registry so RMI can register itself
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            System.out.println("RemoteException occurred while starting the registry.");
            e.printStackTrace();
        }

        for (int i : processIds) {
            String name = "rmi://localhost/DA_BenOr_" + i;
            DA_BenOr da = null;
            try {
                da = new DA_BenOr(i, processIds, remoteProcessIds, remoteHost);
                java.rmi.Naming.bind(name, da);
            } catch (AlreadyBoundException e) {
                System.out.println("AlreadyBoundException occurred while binding object with RMI name: " + name);
                e.printStackTrace();
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException occurred while binding object with RMI name: " + name);
                e.printStackTrace();
            } catch (RemoteException e) {
                System.out.println("RemoteException occurred while binding object with RMI name: " + name);
                e.printStackTrace();
            }

            if (da == null) {
                return;
            }

            // finally start the worker thread of the process
            System.out.println("Starting process node with id=" + i
                    + ", totalProcesses=" + processIds.length);
            new Thread(da, "Thread for process " + i).start();
        }
    }
}
