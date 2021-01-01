/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package freshworks;

import java.io.File;
import java.io.FileNotFoundException;
import org.junit.Test;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ACER
 */
public class DataStoreTest {
    
    DataStore instance;
    static int x;
    static final String FILE_PATH = System.getProperty("user.dir")+"/test";
    static final File FILE;
    static PrintWriter writer ;
    static
    {
        x = (int)(Math.random()*500);  
        FILE = new File(FILE_PATH + "/DataStore.json");
        try
        {
        writer = new PrintWriter(FILE);
        writer.print(""); // clearning contents of test json file
        writer.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not Found. New File would ber created for"
                    + "testing");
        }
    }
  
    public DataStoreTest()throws Exception {
       instance = new DataStore(FILE_PATH);
       //dummy data for the JSON file
       instance.createKey("Create", "Test File");
       instance.createKey("Table", "Chair");
       instance.createKey("Money", "Plant");
       instance.createKey("Hello", "World");
    }
    
    @BeforeAll
    public static void setUpClass() throws Exception {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }
   
    
    /**
     * Test of createKey method, of class DataStore.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateKey_3args() throws Exception {
        System.out.println("createKey Insertion");
        String key = "Rajeev";
        String value = "Shah";
        int expirytime = 1;
        String expResult = "Insertion Success";
        String result = instance.createKey(key, value, expirytime);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of createKey method, of class DataStore for existing key.
     * @throws java.lang.Exception
     */
    
    @Test
    public void testCreateKey_String_String2() throws Exception {
        System.out.println("createKey on already existing key");
        String key = "Table";
        String value = "Chair";
        instance.createKey(key, value);
        //String expResult = "Insertion Success";//use when key does not exist
        String expResult = "Key already exists"; //use when key exists
        String result = instance.createKey(key, value);
        System.out.println(result);
        assertEquals(expResult, result);
    }
    
    /**
     * Checking 32 character Cap on the key
     * @throws java.io.IOException
     */
    
    @Test
    public void testCreateKey_CheckCap() throws Exception {
        System.out.println("createKey 32 Char limit exceeded");
        String key = "nonilfenossipolietilenossietonolo";
        String value = "Longest Word";
        String expResult = "Number of Characters in the key must not exceed 32"; 
        String result = instance.createKey(key, value);
        System.out.println(result);
        assertEquals(expResult, result);
    }
    
    /**
     * Checking 16 KB Cap on the Value
     * @throws java.lang.Exception
     */
    
    @Test
    public void testCreateKey_CheckValueCap() throws Exception {
        System.out.println("createKey size exceeds 16 KB");
        byte [] values = new byte[(16*1024)/6]; // for Json Size > 16 KB
        //byte [] values = new byte[((16*1024)/6) - 20];//for Json Sizes < 16 KB
        String value = new String(values);
        JSONObject obj = new JSONObject(); 
        obj.put("value", value);
        //values = obj.toJSONString().getBytes();
        String key = "CheckingCap";
        String expResult = "Value size exceeds 16 KB. Failed to Insert value"; 
        String result = instance.createKey(key, value);
        System.out.println(result);
        assertEquals(expResult, result);
    }

    /**
     * Test of readKey method, of class DataStore.
     * @throws java.io.IOException
     */
    
    @Test
    public void testReadKey()throws IOException {
        System.out.println("readKey");
        String key = "Hello";
        String value = "World";
        JSONObject expResult = new JSONObject();
        expResult.put(key, value);
        JSONObject result = instance.readKey(key);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of readKey method, of class DataStore using an expired key.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
     @Test
    public void testReadKeyExpired()throws IOException, InterruptedException {
        System.out.println("readKey Expired");
        String key = "Physics";
        String value = "Chemistry";
        instance.createKey(key, value, 2);
        Thread.sleep(5000); // 5 second pause
        JSONObject expResult = new JSONObject();
        JSONObject result = instance.readKey(key);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of readKey method, of class DataStore using an unexpired key.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
     @Test
    public void testReadKeyUnexpired()throws IOException, InterruptedException {
        System.out.println("readKey Unexpired");
        instance.createKey("Glass", "Water", 5);
        Thread.sleep(1000); // 1 second pause
        String key = "Glass";
        String value = "Water";
        JSONObject expResult = new JSONObject();
        expResult.put(key, value);
        JSONObject result = instance.readKey(key);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of readKey method, of class DataStore using a key not present in
     * the JSON file.
     * @throws java.io.IOException
     */
     @Test
    public void testReadKeyAbsentKey()throws IOException {
        System.out.println("readKey Absent Key");
        String key = "Mobile";
        String value = "Phone";
        JSONObject expResult = new JSONObject();
        JSONObject result = instance.readKey(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of deleteKey method, of class DataStore.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteKey() throws Exception {
        System.out.println("deleteKey");
        String key = "Money";
        String expResult = "Deletion Successful";
        String result = instance.deleteKey(key);
        assertEquals(expResult, result);
    }   
    
    /**
     * Test of deleteKey method, of class DataStore for expired key.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteKeyExpired() throws Exception {
        System.out.println("deleteKeyExpired");
        String key = "Adapter";
        instance.createKey(key, "Charger", 1);
        Thread.sleep(2000); // waiting for key to expire
        String expResult = "Key Expired";
        String result = instance.deleteKey(key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }  
    
    /**
     * Test of deleteKey method, of class DataStore for an absent key.
     * @throws java.lang.Exception
     */
    @Test
    public void testDeleteKeyAbsent() throws Exception {
        System.out.println("deleteKeyAbsent");
        String key = "Absent";
        String expResult = "Key does not exist";
        String result = instance.deleteKey(key);
        assertEquals(expResult, result);
    }  
}
