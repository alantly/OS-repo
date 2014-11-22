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
import autograder.AGCategories.AG_PROJ3_CODE;

public class EndToEndTest extends EndToEndTemplate {

    @Test(timeout = kTimeoutDefault)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1, desc = "Testing put and get combining all the pieces")
    public void simplePutGet() {
    	try {
	    	client.put("a","b");
	    	assertEquals(client.get("a"),"b");
	    	//System.out.println("Value of a: " + client.get("a"));	
	    } catch (KVException e) {
	    	fail("unexpected KVException on valid put request");
	    }
    }

    @Test(timeout = kTimeoutDefault)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1, desc = "Testing delete after combining all the pieces")
    public void simpleDel() {
    	try {
    		client.put("del", "menow");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		client.del("del");
    		try { Thread.sleep(100); } catch (InterruptedException ie) {  }
    		client.get("del");
    		fail("Del did not throw a KVException!");
    	} catch (KVException e) {
    		assertEquals(e.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    }

    @Test(timeout = kTimeoutDefault)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1, desc = "Testing put with invalid arguments after combining all the pieces")
    public void invalidPut() {
    	Scanner s2 = null;
    	try {
	    	final String filename = "gobears_maxvalue.txt";
	        InputStream maxValueStream = getClass().getClassLoader().getResourceAsStream(filename);
	        assertNotNull(String.format("Test file not found: %s - Please report to TA", filename), maxValueStream);
	        s2 = new Scanner(maxValueStream);
	        String oversizedValue = s2.nextLine();
	        client.put("never", oversizedValue);
	        fail("Put did not throw a KVException!");
    	} catch (KVException e) {
    		assertEquals(e.getMessage(), KVConstants.ERROR_OVERSIZED_VALUE);
    	}

    }

    @Test(timeout = kTimeoutDefault)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1, desc = "Testing get with invalid key after combining all the pieces")
    public void invalidGet() {
    	try {
    		client.get("idontexist");
    		fail("Client did not throw exception!");
    	} catch (KVException e) {
    		assertEquals(e.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    }

    @Test(timeout = kTimeoutDefault)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1, desc = "Testing del with invalid key after combining all the pieces")
    public void invalidDel() {
    	try {
    		client.del("idontexist");
    		fail("Client did not throw exception!");
    	} catch (KVException e) {
    		assertEquals(e.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
    	}
    }

}
