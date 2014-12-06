package kvstore;

import static kvstore.KVConstants.*;

import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Implements NetworkHandler to handle 2PC operation requests from the Master/
 * Coordinator Server
 */
public class TPCMasterHandler implements NetworkHandler {

    public long slaveID;
    public KVServer kvServer;
    public TPCLog tpcLog;
    public ThreadPool threadpool;

    // implement me

    /**
     * Constructs a TPCMasterHandler with one connection in its ThreadPool
     *
     * @param slaveID the ID for this slave server
     * @param kvServer KVServer for this slave
     * @param log the log for this slave
     */
    public TPCMasterHandler(long slaveID, KVServer kvServer, TPCLog log) {
        this(slaveID, kvServer, log, 1);
    }

    /**
     * Constructs a TPCMasterHandler with a variable number of connections
     * in its ThreadPool
     *
     * @param slaveID the ID for this slave server
     * @param kvServer KVServer for this slave
     * @param log the log for this slave
     * @param connections the number of connections in this slave's ThreadPool
     */
    public TPCMasterHandler(long slaveID, KVServer kvServer, TPCLog log, int connections) {
        this.slaveID = slaveID;
        this.kvServer = kvServer;
        this.tpcLog = log;
        this.threadpool = new ThreadPool(connections);
    }

    /**
     * Registers this slave server with the master.
     *
     * @param masterHostname
     * @param server SocketServer used by this slave server (which contains the
     *               hostname and port this slave is listening for requests on
     * @throws KVException with ERROR_INVALID_FORMAT if the response from the
     *         master is received and parsed but does not correspond to a
     *         success as defined in the spec OR any other KVException such
     *         as those expected in KVClient in project 3 if unable to receive
     *         and/or parse message
     */
    public void registerWithMaster(String masterHostname, SocketServer server)
            throws KVException {
        // implement me
        try {
            String msg = Long.toString(slaveID) + "@" + server.getHostname() + ":" + Integer.toString(server.getPort());
            KVMessage kvm = new KVMessage(REGISTER, msg);
            Socket s = new Socket(masterHostname, 9090);
            kvm.sendMessage(s);
            KVMessage response = new KVMessage(s);
            if (!response.getMsgType().equals(RESP) || response.getMessage().equals("Unsuccessful registration "+ msg)) {
                throw new KVException(ERROR_INVALID_FORMAT);
            }
        } catch (UnknownHostException uhe) {
            throw new KVException(ERROR_COULD_NOT_CREATE_SOCKET);

        } catch (IOException ioe) {
            throw new KVException(ERROR_COULD_NOT_CREATE_SOCKET);
        }
    }
    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param master Socket connected to the master with the request
     */
    @Override
    public void handle(Socket master) {
        TCPMasterHandlerRunner job = new TCPMasterHandlerRunner(master, this.kvServer, this.tpcLog);
        try {
            this.threadpool.addJob(job);
        }
        catch (InterruptedException e) {}
    }

    public class TCPMasterHandlerRunner implements Runnable {
        private Socket master;
        private KVServer kvServer;
        private TPCLog tpcLog;

        public TCPMasterHandlerRunner(Socket master, KVServer kvServer, TPCLog tpcLog) {
            this.master = master;
            this.kvServer = kvServer;
            this.tpcLog = tpcLog;
        }

        @Override
        public void run() {
            KVMessage response_kvm = new KVMessage(RESP, SUCCESS);
            try {
                KVMessage kvm = new KVMessage(master);
                // add every msg to our log file  
                this.tpcLog.appendAndFlush(kvm);
                if (kvm.getMsgType().equals(GET_REQ)) {
                    String value = kvServer.get(kvm.getKey());
                    response_kvm.setMessage(null);
                    response_kvm.setKey(kvm.getKey());
                    response_kvm.setValue(value);
                }else if (kvm.getMsgType().equals(DEL_REQ)) {
                    kvServer.del(kvm.getKey());
                } else if (kvm.getMsgType().equals(PUT_REQ)) {
                    kvServer.put(kvm.getKey(), kvm.getValue());
                }
                response_kvm.sendMessage(master);
            }
            catch (KVException kve) {
                response_kvm = kve.getKVMessage();
                try {
                    response_kvm.sendMessage(master);
                }
                catch (KVException e) {}
            }
        }
    }
}
