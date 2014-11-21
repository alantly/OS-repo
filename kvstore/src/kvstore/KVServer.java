package kvstore;

import static kvstore.KVConstants.ERROR_OVERSIZED_KEY;
import static kvstore.KVConstants.ERROR_OVERSIZED_VALUE;
import static kvstore.KVConstants.RESP;

import java.util.concurrent.locks.Lock;

/**
 * This class services all storage logic for an individual key-value server.
 * All KVServer request on keys from different sets must be parallel while
 * requests on keys from the same set should be serial. A write-through
 * policy should be followed when a put request is made.
 */
public class KVServer implements KeyValueInterface {

    private KVStore dataStore;
    private KVCache dataCache;

    private static final int MAX_KEY_SIZE = 256;
    private static final int MAX_VAL_SIZE = 256 * 1024;

    /**
     * Constructs a KVServer backed by a KVCache and KVStore.
     *
     * @param numSets the number of sets in the data cache
     * @param maxElemsPerSet the size of each set in the data cache
     */

    public KVServer(int numSets, int maxElemsPerSet) {
        this.dataCache = new KVCache(numSets, maxElemsPerSet);
        this.dataStore = new KVStore();
    }

    /**
     * Performs put request on cache and store.
     *
     * @param  key String key
     * @param  value String value
     * @throws KVException if key or value is too long
     */
    @Override
    public void put(String key, String value) throws KVException {
        // implement me
        // can get lock from this.dataCache.getLock(key)
        if (key.length() > MAX_KEY_SIZE)
            throw new KVException(KVConstants.ERROR_OVERSIZED_KEY);
        else if (value.length() > MAX_VAL_SIZE)
            throw new KVException(KVConstants.ERROR_OVERSIZED_VALUE);

        Lock cachelock = this.dataCache.getLock(key);

        try {     
            cachelock.lock();
            this.dataStore.put(key, value);
            this.dataCache.put(key, value);
        }
        finally {
            cachelock.unlock();
        }
    }

    /**
     * Performs get request.
     * Checks cache first. Updates cache if not in cache but located in store.
     *
     * @param  key String key
     * @return String value associated with key
     * @throws KVException with ERROR_NO_SUCH_KEY if key does not exist in store
     */
    @Override
    public String get(String key) throws KVException {
        // implement me
        String cacheValue = null;
        String storeValue = null;
        String returnValue = null;

        Lock cachelock = this.dataCache.getLock(key);

        try {
            cachelock.lock();
            cacheValue = this.dataCache.get(key);
            if (cacheValue == null) {
                storeValue = this.dataStore.get(key);
                this.dataCache.put(key, storeValue);
                returnValue = storeValue;
            }
            else
                returnValue = cacheValue;
        }
        catch (KVException kve) {
            throw kve;
        }
        finally {

            cachelock.unlock();
        }

        return returnValue;
    }

    /**
     * Performs del request.
     *
     * @param  key String key
     * @throws KVException with ERROR_NO_SUCH_KEY if key does not exist in store
     */
    @Override
    public void del(String key) throws KVException {
        // implement me
        Lock cachelock = this.dataCache.getLock(key);

        try {
            cachelock.lock();
            this.dataStore.del(key);
            this.dataCache.del(key);
        }
        catch (KVException kve) {
            throw kve;
        }
        finally {
            cachelock.unlock();
        }
    }

    /**
     * Check if the server has a given key. This is used for TPC operations
     * that need to check whether or not a transaction can be performed but
     * you don't want to modify the state of the cache by calling get(). You
     * are allowed to call dataStore.get() for this method.
     *
     * @param key key to check for membership in store
     */
    public boolean hasKey(String key) {
        String value = null;
        try {
            value = this.dataStore.get(key);
        }
        catch (KVException kve) {}
        
        return !(value == null);
    }

    /** This method is purely for convenience and will not be tested. */
    @Override
    public String toString() {
        return dataStore.toString() + dataCache.toString();
    }

}