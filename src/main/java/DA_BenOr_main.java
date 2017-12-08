import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * Created by jeroen on 12/8/17.
 */
public class DA_BenOr_main {
    public static void main(String... args) {
        OptionParser parser = new OptionParser();

        parser.accepts("t", "Total number of processes in DS")
                .withRequiredArg().ofType(Integer.class);
        parser.accepts("i", "ID of this process")
                .withRequiredArg().ofType(String.class);
        parser.accepts("o", "IDs of other processes in DS")
                .withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        if (!options.has("i") || !options.has("t") || !options.has("o")) {
            return;
        }

        // parse the options
        int totalProcesses;
        totalProcesses = (Integer) options.valueOf("t");
        int id = (Integer) options.valueOf("i");
        String ids = (String) options.valueOf("o");
        int[] processIds = Arrays.stream(ids.split(" "))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();

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

        String name = "rmi://localhost/DA_BenOr_" + id;
        DA_BenOr da = null;
        try {
            da = new DA_BenOr(id, processIds);

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
        System.out.println("Starting process node with id=" + id
                + ", totalProcesses=" + totalProcesses);
        new Thread(da, "Thread for process " + id).start();
    }
}
