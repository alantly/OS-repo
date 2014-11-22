package kvstore;

import static kvstore.KVConstants.DEL_REQ;
import static kvstore.KVConstants.GET_REQ;
import static kvstore.KVConstants.PUT_REQ;
import static kvstore.KVConstants.RESP;
import static kvstore.KVConstants.SUCCESS;

import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * Uses a thread pool to ensure that none of its methods are blocking.
 */
public class ServerClientHandler implements NetworkHandler {

    public KVServer kvServer;
    public ThreadPool threadPool;

    /**
     * Constructs a ServerClientHandler with ThreadPool of a single thread.
     *
     * @param kvServer KVServer to carry out requests
     */
    public ServerClientHandler(KVServer kvServer) {
        this(kvServer, 1);
    }

    /**
     * Constructs a ServerClientHandler with ThreadPool of thread equal to
     * the number passed in as connections.
     *
     * @param kvServer KVServer to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public ServerClientHandler(KVServer kvServer, int connections) {
        // implement me
        this.threadPool = new ThreadPool(connections);
        this.kvServer = kvServer;
    }

    /**
     * Creates a job to service the request for a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param client Socket connected to the client with the request
     */
    @Override
    public void handle(Socket client) {
        // implement me
        ServerClientHandlerRunner job = new ServerClientHandlerRunner(client, kvServer);
        try {
            threadPool.addJob(job);
        }
        catch (InterruptedException e) {
            // ignore any InterruptedExceptions like suggested above
        }
        
    }

    public class ServerClientHandlerRunner implements Runnable {
        private Socket client;
        private KVServer kvServer;

        public ServerClientHandlerRunner(Socket client, KVServer kvServer) {
            this.client = client;
            this.kvServer = kvServer;
        }
        // implement me
        @Override
        public void run() {
            KVMessage response_kvm = new KVMessage(RESP, SUCCESS);
            try {
                KVMessage kvm = new KVMessage(client);
                if (kvm.getMsgType().equals(DEL_REQ)) {
                    kvServer.del(kvm.getKey());
                } else if (kvm.getMsgType().equals(GET_REQ)) {
                    String value = kvServer.get(kvm.getKey());
                    response_kvm.setMessage(null);
                    response_kvm.setKey(kvm.getKey());
                    response_kvm.setValue(value);
                } else if (kvm.getMsgType().equals(PUT_REQ)) {
                    kvServer.put(kvm.getKey(), kvm.getValue());
                }
                response_kvm.sendMessage(client);
            } catch (KVException kve) {
                response_kvm = kve.getKVMessage();
                try {
                    response_kvm.sendMessage(client);
                } catch (KVException e) {}
            }        
        }
    }

}
