package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * It uses a threadPool to ensure that none of it's methods are blocking.
 */
public class TPCClientHandler implements NetworkHandler {

    public TPCMaster tpcMaster;
    public ThreadPool threadPool;

    /**
     * Constructs a TPCClientHandler with ThreadPool of a single thread.
     *
     * @param tpcMaster TPCMaster to carry out requests
     */
    public TPCClientHandler(TPCMaster tpcMaster) {
        this(tpcMaster, 1);
    }

    /**
     * Constructs a TPCClientHandler with ThreadPool of a single thread.
     *
     * @param tpcMaster TPCMaster to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public TPCClientHandler(TPCMaster tpcMaster, int connections) {
        // implement me
        this.tpcMaster = tpcMaster;
        threadPool = new ThreadPool(connections);
    }

    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore InterruptedExceptions.
     *
     * @param client Socket connected to the client with the request
     */
    @Override
    public void handle(Socket client) {
        // implement me
        TPCClientHandlerRunner job = new TPCClientHandlerRunner(client, tpcMaster);
        try {
            threadPool.addJob(job);
        }
        catch (InterruptedException e) {
            // ignore any InterruptedExceptions like suggested above
        }
    }
    
    public class TPCClientHandlerRunner implements Runnable {
        private Socket client;
        private TPCMaster master;

        public TPCClientHandlerRunner(Socket client, TPCMaster master) {
            this.client = client;
            this.master = master;
        }
        // implement me
        @Override
        public void run() {
            KVMessage response = new KVMessage(RESP,SUCCESS);
            try {
                KVMessage kvm = new KVMessage(client);
                if (kvm.getMsgType.equals(GET_REQ)) {
                    String value = master.handleGET(kvm);
                    response.setValue(value);
                    response.setKey(kvm.getKey());
                    response.setMessage(null);
                    response.sendMessage(client);
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
