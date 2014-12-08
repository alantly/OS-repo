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
    		client.put("amazonID", "product");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		assertEquals(client.get("amazonID"), "product");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/get request");
    	}
    }
    
    @Test(timeout = 3000)
    public void invalidGet() throws KVException {
    	try {
    		client.get("invalidKey");
    		fail("Should have thrown an exception.");
    	} catch (KVException k) {
    		assertEquals(k.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    }

    @Test(timeout = 30000)
    public void simplePutDel() {
    	try {
    		client.put("steamID", "gameTitle");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		client.del("steamID");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/del request.");
    	}
    }

    @Test(timeout = 30000)
    public void invalidDel() throws KVException {
    	try {
    		client.del("amazonID");
    		fail("Should have thrown an exception.");	
    	} catch (KVException k) {
    		assertEquals(k.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    }

    @Test(timeout = kTimeoutDefault)
    public void invalidPut() {
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
    }

    @Test(timeout = kTimeoutDefault)
    public void multipleClient() {
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
    }

    @Test(timeout = kTimeoutDefault)
    public void threeClientRequest() {
    	try {
    		client.put("amazon1", "product1");
    		client2.put("amazon2", "product2");
    		client3.put("amazon3", "product3");
    		//try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		assertEquals(client4.get("amazon2"), "product2");
    		assertEquals(client4.get("amazon1"), "product1");
    		assertEquals(client4.get("amazon3"), "product3");
    	} catch (KVException k) {
    		fail("Unexpected KVException on valid put/get request");
    	}
    } 
}
