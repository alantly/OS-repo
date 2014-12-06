package kvstore;

import static kvstore.KVConstants.*;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;


public class TPCMaster {

    public int numSlaves;
    public KVCache masterCache;
    private ArrayList<TPCSlaveInfo> slaves;
    private TPCSlaveInfo deadSlave;
    public HashSet<Long> slaveIDs;
    final Lock registerLock;
    final Condition hasFinishedRegistration;

    public static final int TIMEOUT = 3000;

    /**
     * Creates TPCMaster, expecting numSlaves slave servers to eventually register
     *
     * @param numSlaves number of slave servers expected to register
     * @param cache KVCache to cache results on master
     */
    public TPCMaster(int numSlaves, KVCache cache) {
        this.numSlaves = numSlaves;
        this.masterCache = cache;
        // implement me
        this.deadSlave = null;
        this.slaves = new ArrayList<TPCSlaveInfo>();
        this.slaveIDs = new HashSet<Long>();
        this.registerLock = new ReentrantLock();
        this.hasFinishedRegistration = registerLock.newCondition();
    }

    /**
     * Registers a slave. Drop registration request if numSlaves already
     * registered. Note that a slave re-registers under the same slaveID when
     * it comes back online.
     *
     * @param slave the slaveInfo to be registered
     */
    public void registerSlave(TPCSlaveInfo slave) {
        // implement me
        this.registerLock.lock();
        if (deadSlave != null && slave.getSlaveID() == deadSlave.getSlaveID()) {
            deadSlave = null;
        } else if (slaves.size() == numSlaves || slaveIDs.contains(slave.getSlaveID())) {
            // doing nothing if we already have numSlaves or a slave is trying to reregister even though it is not dead.
        } else {
            long slave_id = slave.getSlaveID();
            if (slaves.size() == 0) {
                slaves.add(slave);
                slaveIDs.add(slave_id);
            } else {
                if (isLessThanUnsigned(slave_id,slaves.get(0).getSlaveID())) {
                    slaves.add(0, slave);
                    slaveIDs.add(slave_id);
                } else if (!isLessThanEqualUnsigned(slave_id, slaves.get(slaves.size() - 1).getSlaveID())) {
                    slaves.add(slave);
                    slaveIDs.add(slave_id);
                } else {
                    for (int i = 0; i < slaves.size() - 1; i ++) {
                        long previous_id = slaves.get(i).getSlaveID();
                        long next_id = slaves.get(i + 1).getSlaveID();
                        if (isLessThanUnsigned(previous_id, slave_id) && isLessThanUnsigned(slave_id, next_id)) {
                            slaves.add(i+1, slave);
                            slaveIDs.add(slave_id);
                        }
                    }
                }
            }
        }

        this.hasFinishedRegistration.signalAll();
        this.registerLock.unlock();
        return;
    }

    /**
     * Converts Strings to 64-bit longs. Borrowed from http://goo.gl/le1o0W,
     * adapted from String.hashCode().
     *
     * @param string String to hash to 64-bit
     * @return long hashcode
     */
    public static long hashTo64bit(String string) {
        long h = 1125899906842597L;
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = (31 * h) + string.charAt(i);
        }
        return h;
    }

    /**
     * Compares two longs as if they were unsigned (Java doesn't have unsigned
     * data types except for char). Borrowed from http://goo.gl/QyuI0V
     *
     * @param n1 First long
     * @param n2 Second long
     * @return is unsigned n1 less than unsigned n2
     */
    public static boolean isLessThanUnsigned(long n1, long n2) {
        return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    /**
     * Compares two longs as if they were unsigned, uses isLessThanUnsigned
     *
     * @param n1 First long
     * @param n2 Second long
     * @return is unsigned n1 less than or equal to unsigned n2
     */
    public static boolean isLessThanEqualUnsigned(long n1, long n2) {
        return isLessThanUnsigned(n1, n2) || (n1 == n2);
    }

    /**
     * Find primary replica for a given key.
     *
     * @param key String to map to a slave server replica
     * @return SlaveInfo of first replica
     */
    public TPCSlaveInfo findFirstReplica(String key) {
        // implement me
        long hashed_key = hashTo64bit(key);
        if (isLessThanUnsigned(hashed_key,slaves.get(0).getSlaveID())) {
            return slaves.get(0);
        } else if (!isLessThanEqualUnsigned(hashed_key, slaves.get(slaves.size() - 1).getSlaveID())) {
            return slaves.get(0);
        } else {
            for (int i = 0; i < slaves.size() - 1; i ++) {
                long previous_id = slaves.get(i).getSlaveID();
                long next_id = slaves.get(i + 1).getSlaveID();
                if (isLessThanUnsigned(previous_id, hashed_key) && isLessThanUnsigned(hashed_key, next_id)) {
                    return slaves.get(i+1);
                }
            }
        }
        return null;
    }

    /**
     * Find the successor of firstReplica.
     *
     * @param firstReplica SlaveInfo of primary replica
     * @return SlaveInfo of successor replica
     */
    public TPCSlaveInfo findSuccessor(TPCSlaveInfo firstReplica) {
        long hashed_key = firstReplica.getSlaveID();
        if (!isLessThanUnsigned(hashed_key, slaves.get(slaves.size() - 1).getSlaveID())) {
            return slaves.get(0);
        } else {
            for (int i = 0; i < slaves.size() - 1; i ++) {
                long previous_id = slaves.get(i).getSlaveID();
                long next_id = slaves.get(i + 1).getSlaveID();
                if (isLessThanEqualUnsigned(previous_id, hashed_key) && isLessThanUnsigned(hashed_key, next_id)) {
                    return slaves.get((i+1)%numSlaves);
                }
            }
        }
        return null;
    }

    /**
     * @return The number of slaves currently registered.
     */
    public int getNumRegisteredSlaves() {
        if (deadSlave != null) {
            return slaves.size() - 1;
        }
        return slaves.size();
    }

    /**
     * (For testing only) Attempt to get a registered slave's info by ID.
     * @return The requested TPCSlaveInfo if present, otherwise null.
     */
    public TPCSlaveInfo getSlave(long slaveId) {
        // implement me
        return null;
    }

    /**
     * Perform 2PC operations from the master node perspective. This method
     * contains the bulk of the two-phase commit logic. It performs phase 1
     * and phase 2 with appropriate timeouts and retries.
     *
     * See the spec for details on the expected behavior.
     *
     * @param msg KVMessage corresponding to the transaction for this TPC request
     * @param isPutReq boolean to distinguish put and del requests
     * @throws KVException if the operation cannot be carried out for any reason
     */
    public synchronized void handleTPCRequest(KVMessage msg, boolean isPutReq)
            throws KVException {
        // implement me
        try {
            blockUntilRegisterComplete();
        } catch (InterruptedException ite) {}        

        if (isPutReq) {
            String key = msg.getKey();
            TPCSlaveInfo slave1 = findFirstReplica(key);
            TPCSlaveInfo slave2 = findSuccessor(slave1);
            //do TPC
        } else {

        }

    }

    /**
     * Perform GET operation in the following manner:
     * - Try to GET from cache, return immediately if found
     * - Try to GET from first/primary replica
     * - If primary succeeded, return value
     * - If primary failed, try to GET from the other replica
     * - If secondary succeeded, return value
     * - If secondary failed, return KVExceptions from both replicas
     *
     * @param msg KVMessage containing key to get
     * @return value corresponding to the Key
     * @throws KVException with ERROR_NO_SUCH_KEY if unable to get
     *         the value from either slave for any reason
     */
    public String handleGet(KVMessage msg) throws KVException {
        // implement me

        String msgkey = msg.getKey();
        String msgtype = msg.getMsgType();
        String value = null;
        String response_msg;
        TPCSlaveInfo slave1 = null;
        
        try {
            blockUntilRegisterComplete();
        } catch (InterruptedException ite) {}

        Lock cacheLock = this.masterCache.getLock(msgkey);
        // Try getting from the masterCache
        cacheLock.lock();
        value = this.masterCache.get(msgkey);
        cacheLock.unlock();

        Socket nSocket = null;
        KVMessage response = null;

        if (value != null)
            return value;
        // Try getting from first slave
        slave1 = this.findFirstReplica(msgkey);
        nSocket = slave1.connectHost(TIMEOUT);
        msg.sendMessage(nSocket);
        response = new KVMessage(nSocket, TIMEOUT);
        value = response.getValue();
        slave1.closeHost(nSocket);
        response_msg = response.getMessage();

        if (response_msg != null && response_msg.equals(ERROR_NO_SUCH_KEY)) {
            throw new KVException(response);
        } else if (response_msg != null && response_msg.equals(ERROR_COULD_NOT_CONNECT)) {
            //first replica is dead, mark as dead.
            deadSlave = slave1; 
            // Try getting from second slave
            TPCSlaveInfo slave2 = this.findSuccessor(slave1);
            nSocket = slave2.connectHost(TIMEOUT);
            msg.sendMessage(nSocket);
            response = new KVMessage(nSocket, TIMEOUT);
            value = response.getValue();
        }
        
        if (response_msg != null && response_msg.equals(ERROR_NO_SUCH_KEY)) {
            throw new KVException(response);
        } else {
            // Update MasterCache
            try {
            cacheLock.lock();
            this.masterCache.put(msgkey, value);
            } finally {
                cacheLock.unlock();
            }
        }
        return value; 
        
    }
    /**
     * Performs a wait until numSlaves slaves have registered.
     *
     * @throws InterruptedException if a CPU interrupt occurs. Check Java API for more details
     */
    private void blockUntilRegisterComplete() throws InterruptedException {
        this.registerLock.lock();
        while (this.slaves.size() < numSlaves) {
            this.hasFinishedRegistration.await();
        }
        this.registerLock.unlock();
    }
}
