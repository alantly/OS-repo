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
    public void aTest() {
        System.out.println("Test");
    }

}
