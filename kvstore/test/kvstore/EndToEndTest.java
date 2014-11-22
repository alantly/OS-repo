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

import org.junit.Test;
import org.junit.experimental.categories.Category;

import autograder.AGCategories.AGTestDetails;
import autograder.AGCategories.AG_PROJ3_CODE;

public class EndToEndTest extends EndToEndTemplate {

    @Test(timeout = kTimeoutDefault)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1, desc = "")
    public void simplePutGet() {
    	try {
	    	client.put("a","b");
	    	assertEquals(client.get("a"),"b");
	    	//System.out.println("Value of a: " + client.get("a"));	
	    } catch (KVException e) {
	    	System.out.println("Exception: " + e.getMessage());
	    }
    }

}
