package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * Uses a thread pool to ensure that none of its methods are blocking.
 */
public class TPCRegistrationHandler implements NetworkHandler {

    private ThreadPool threadpool;
    private TPCMaster master;

    /**
     * Constructs a TPCRegistrationHandler with a ThreadPool of a single thread.
     *
     * @param master TPCMaster to register slave with
     */
    public TPCRegistrationHandler(TPCMaster master) {
        this(master, 1);
    }

    /**
     * Constructs a TPCRegistrationHandler with ThreadPool of thread equal to the
     * number given as connections.
     *
     * @param master TPCMaster to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public TPCRegistrationHandler(TPCMaster master, int connections) {
        this.threadpool = new ThreadPool(connections);
        this.master = master;
    }

    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param slave Socket connected to the slave with the request
     */
    @Override
    public void handle(Socket slave) {
        // implement me
        TPCRegistrationHandlerRunner job = new TPCRegistrationHandlerRunner(slave, master);
        try {
            threadpool.addJob(job);
        }
        catch (InterruptedException e) {
            // ignore any InterruptedExceptions like suggested above
        }
    }
    

    public class TPCRegistrationHandlerRunner implements Runnable {
        private Socket client;
        private TPCMaster master;

        public TPCRegistrationHandlerRunner(Socket client, TPCMaster master) {
            this.client = client;
            this.master = master;
        }
        // implement me
        @Override
        public void run() {
            KVMessage response_kvm = new KVMessage(RESP);
            try {
                KVMessage kvm = new KVMessage(client);
                if (kvm.getMsgType().equals(REGISTER)) {
                    String msg = kvm.getMessage();
                    TPCSlaveInfo slaveInfo = new TPCSlaveInfo(msg);
                    master.registerSlave(slaveInfo);
                    response_kvm.setMessage("Successfully registered "+ msg);
                    if (!master.slaveMap.containsKey(slaveInfo.getSlaveID())) 
                        response_kvm.setMessage("Unsuccessful registration "+ msg);
                    response_kvm.sendMessage(client);
                }
            } catch (KVException kve) {
                response_kvm = kve.getKVMessage();
                try {
                    response_kvm.sendMessage(client);
                } catch (KVException e) {}
            }        
        }

    }
}
