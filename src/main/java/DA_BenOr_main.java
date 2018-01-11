import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by jeroen on 12/8/17.
 * Run with the following VM options:
 * -Djava.security.policy=my.policy -Djava.rmi.server.hostname=192.168.56.101
 */
public class DA_BenOr_main {
    public static void main(String... args) {
        int processIds[] = IntStream.range(1, 11).toArray();
        int remoteProcessIds[] = IntStream.range(11, 21).toArray();
        int fractionMalicious = 20;
        String remoteHost = "145.94.152.27";

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
            boolean malicious = new Random().nextFloat() < (1.00 / fractionMalicious);
            String name = "rmi://localhost:1099/DA_BenOr_" + i;
            DA_BenOr da = null;
            try {
                da = new DA_BenOr(i, processIds, remoteProcessIds, fractionMalicious, remoteHost, malicious);
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
