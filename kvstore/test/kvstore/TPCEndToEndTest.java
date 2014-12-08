package kvstore;

import static autograder.TestUtils.*;
import static kvstore.KVConstants.*;
import static kvstore.Utils.assertKVExceptionEquals;
import static kvstore.Utils.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Random;

import kvstore.Utils.ErrorLogger;
import kvstore.Utils.RandomString;

import java.io.InputStream;
import java.util.Scanner;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import autograder.AGCategories.AGTestDetails;
import autograder.AGCategories.AG_PROJ4_CODE;

public class TPCEndToEndTest extends TPCEndToEndTemplate {

    @Test(timeout = 3000)
    public void simplePutGet() {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	try {
    		client.put("amazonID", "product");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		assertEquals(client.get("amazonID"), "product");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/get request");
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }
    
    @Test(timeout = 3000)
    public void invalidGet() throws KVException {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	try {
    		client.get("invalidKey");
    		fail("Should have thrown an exception.");
    	} catch (KVException k) {
    		assertEquals(k.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }

    @Test(timeout = 30000)
    public void simplePutDel() {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	try {
    		client.put("steamID", "gameTitle");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		client.del("steamID");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/del request.");
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }

    @Test(timeout = 30000)
    public void invalidDel() throws KVException {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	try {
    		client.del("amazonID");
    		fail("Should have thrown an exception.");	
    	} catch (KVException k) {
    		assertEquals(k.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }

    @Test(timeout = kTimeoutDefault)
    public void invalidPut() {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	Scanner s2 = null;
    	try {
	    	final String filename = "gobears_maxvalue.txt";
	        InputStream maxValueStream = getClass().getClassLoader().getResourceAsStream(filename);
	        assertNotNull(String.format("Test file not found: %s - Please report to TA", filename), maxValueStream);
	        s2 = new Scanner(maxValueStream);
	        String oversizedValue = s2.nextLine();
	        client.put("never", oversizedValue);
	        fail("Put did not throw a KVException.");
    	} catch (KVException e) {
    		assertEquals(e.getMessage(), KVConstants.ERROR_OVERSIZED_VALUE);
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }

    @Test(timeout = kTimeoutDefault)
    public void multipleClient() {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	try {
    		client.put("amazonID", "product");
    		client.put("amazonID2", "wishlist");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		client.get("amazonID");
    		client2.get("amazonID2");
    		assertEquals(client.get("amazonID"), "product");
    		assertEquals(client.get("amazonID2"), "wishlist");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/get request");
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }

    @Test(timeout = kTimeoutDefault)
    public void threeClientRequest() {
    	try {
    		setUp(0);
    	} catch (Exception e) {}
    	try {
    		client.put("amazon1", "product1");
    		client2.put("amazon2", "product2");
    		client3.put("amazon3", "product3");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		assertEquals(client4.get("amazon2"), "product2");
    		assertEquals(client4.get("amazon1"), "product1");
    		assertEquals(client4.get("amazon3"), "product3");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/get request");
    	}
    	try {
    		tearDown();
    	} catch (InterruptedException i) {}
    }

    // This tests whether we get from the masterCache or the slaves
    // The slaves will not have any key values stored in them
    // The masterCache will be initialized with its cache filled with a key, value pair
    @Test(timeout = 30000)
    public void handleGetFromMasterCache() {
        try {
    		setUp(1);
    	} catch (Exception e) {}
        try {
            assertEquals(client.get("cloudID"), "pictures");
        } catch (KVException k) {
            fail("Should have gotten a value from masterCache");
        }
        try {
    		tearDown();
    	} catch (InterruptedException i) {}
    } 
}
