package kvstore;

import java.net.InetAddress;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import java.io.File;

public class TPCEndToEndTemplate {

    String hostname;
    KVClient client;
    KVClient client2;
    KVClient client3;
    KVClient client4;
    TPCMaster master;
    ServerRunner masterClientRunner;
    ServerRunner masterSlaveRunner;
    HashMap<String, ServerRunner> slaveRunners;

    static final int CLIENTPORT = 8888;
    static final int SLAVEPORT = 9090;

    static final int NUMSLAVES = 4;

    static final long SLAVE1 = 4611686018427387903L;  // Long.MAX_VALUE/2
    static final long SLAVE2 = 9223372036854775807L;  // Long.MAX_VALUE
    static final long SLAVE3 = -4611686018427387903L; // Long.MIN_VALUE/2
    static final long SLAVE4 = -0000000000000000001;  // Long.MIN_VALUE

    static final String KEY1 = "6666666666666666666"; // 2846774474343087985
    static final String KEY2 = "9999999999999999999"; // 8204764838124603412
    static final String KEY3 = "0000000000000000000"; //-7869206253219942869
    static final String KEY4 = "3333333333333333333"; //-2511215889438427442

    public void setUp(int masterOption, int slaveOption) throws Exception {
        hostname = InetAddress.getLocalHost().getHostAddress();

        startMaster(masterOption);

        slaveRunners = new HashMap<String, ServerRunner>();
        switch(slaveOption) {
            case 1: //Setup slave with key, value
                startSlave(SLAVE1, 0);
                startSlave(SLAVE2, 0);
                startSlave(SLAVE3, slaveOption);
                startSlave(SLAVE4, 0);
                break;
            default:
                startSlave(SLAVE1, slaveOption);
                startSlave(SLAVE2, slaveOption);
                startSlave(SLAVE3, slaveOption);
                startSlave(SLAVE4, slaveOption);
                break;
        }

        client = new KVClient(hostname, CLIENTPORT);
        client2 = new KVClient(hostname, CLIENTPORT);
        client3 = new KVClient(hostname, CLIENTPORT);
        client4 = new KVClient(hostname, CLIENTPORT);
    }

    public void tearDown() throws InterruptedException {
        masterClientRunner.stop();
        masterSlaveRunner.stop();

        for (ServerRunner slaveRunner : slaveRunners.values()) {
            slaveRunner.stop();
        }

        client = null;
        client2 = null;
        client3 = null;
        client4 = null;
        master = null;
        slaveRunners = null;
    }

    protected void startMaster(int startOption) throws Exception {
        switch(startOption) {
            case 1: // Start with masterCache having one key, value
                KVCache masterCache = new KVCache(1, 4);
                masterCache.put("cloudID", "pictures");
                master = new TPCMaster(NUMSLAVES, masterCache);
                break;

            default:
                master = new TPCMaster(NUMSLAVES, new KVCache(1,4));
                break;
        }
        SocketServer clientSocketServer = new SocketServer(hostname, CLIENTPORT);
        clientSocketServer.addHandler(new TPCClientHandler(master));
        masterClientRunner = new ServerRunner(clientSocketServer, "masterClient");
        masterClientRunner.start();
        SocketServer slaveSocketServer = new SocketServer(hostname, SLAVEPORT);
        slaveSocketServer.addHandler(new TPCRegistrationHandler(master));
        masterSlaveRunner = new ServerRunner(slaveSocketServer, "masterSlave");
        masterSlaveRunner.start();
        Thread.sleep(100);
    }

    protected void startSlave(long slaveID, int slaveOption) throws Exception {
        String name = new Long(slaveID).toString();
        ServerRunner sr = slaveRunners.get(name);
        if (sr != null) {
            sr.start();
            return;
        }

        SocketServer ss = new SocketServer(InetAddress.getLocalHost().getHostAddress(), 0);
        KVServer slaveKvs = new KVServer(100, 10);
    	Long id = new Long(slaveID);
    	File temp = File.createTempFile(id.toString() + "calbandgreat",".txt");
    	temp.deleteOnExit();
        String logPath = temp.getPath(); //"bin/log." + slaveID + "@" + ss.getHostname();
        TPCLog log = new TPCLog(logPath, slaveKvs);
        switch(slaveOption) {
            case 1:
                slaveKvs.put("cloudID", "files");
                break;

            default:
                break;
        }
        TPCMasterHandler handler = new TPCMasterHandler(slaveID, slaveKvs, log);
        ss.addHandler(handler);
        ServerRunner slaveRunner = new ServerRunner(ss, name);
        slaveRunner.start();
        slaveRunners.put(name, slaveRunner);

        handler.registerWithMaster(InetAddress.getLocalHost().getHostAddress(), ss);
    }

    protected void stopSlave(String name) throws InterruptedException {
        ServerRunner sr = slaveRunners.get(name);
        if (sr == null) {
            throw new RuntimeException("Slave does not exist!");
        } else {
            sr.stop();
        }
    }

}
