package kvstore;

import static autograder.TestUtils.kTimeoutQuick;
import static kvstore.KVConstants.ERROR_NO_SUCH_KEY;
import static kvstore.KVConstants.RESP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import autograder.AGCategories.AGTestDetails;
import autograder.AGCategories.AG_PROJ3_CODE;

public class KVStoreTest {

    KVStore store;

    @Before
    public void setupStore() {
        store = new KVStore();
    }

    @Test(timeout = kTimeoutQuick)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1,
        desc = "Verify get returns value just put into store")
    public void putAndGetOneKey() throws KVException {
        String key = "this is the key.";
        String val = "this is the value.";
        store.put(key, val);
        assertEquals(val, store.get(key));
    }

    @Test(timeout = kTimeoutQuick)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1,
        desc = "Test dump files")
    public void testDumpfiles() throws KVException {
        store.put("key1", "value1");
        store.put("key2", "value2");
        store.put("key3", "value3");
        store.dumpToFile("test.txt");
    }

    @Test(timeout = kTimeoutQuick)
    @Category(AG_PROJ3_CODE.class)
    @AGTestDetails(points = 1,
        desc = "Test read from dumpfiles and store to the store")
    public void testReadFromDumpfiles() throws KVException {
       store.restoreFromFile("test.txt");
       assertEquals("value1", store.get("key1"));
       assertEquals("value2", store.get("key2"));
       assertEquals("value3", store.get("key3"));
    }
}
