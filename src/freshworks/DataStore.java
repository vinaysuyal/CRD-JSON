package freshworks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataStore
{
    String FilePath;//Path to the file
    File file;
    int FileSize;//Maximum size of the file
    static JSONObject jObject;//Json Object representing the JSON File
    int valuecap;//max value of the JSON value
    int file_size;
    
    static FileOutputStream fos;
    static FileLock fl;
    DataStore()throws IOException // Defaut Constructor when no path is defined
    {
        this.FileSize = 1024*1024; // Capped at 1024 * 1024 Bytes = 1 GB
        this.valuecap = 16 * 1024; // Capped at 16 KB
        this.FilePath = System.getProperty("user.dir"); 
        //default path same as the user directory 
        file = new File(this.FilePath+"/DataStore.json");
        file.createNewFile(); // Creare a new file if not present already
        readJson();
        
        fos = new FileOutputStream(file, true);
        if(! (fl==null))
        fl.release();
        fl = fos.getChannel().lock();
    }

    DataStore(String FilePath)throws IOException //Constructor when user wishes to set a custom location for JSON File 
    {
        this.FileSize = 1024 * 1024 * 1024; // Capped at 1024 * 1024 * 1024 Bytes = 1 GB
        this.valuecap = 16 * 1024; // Capped at 16 KB
        this.FilePath = FilePath;
        file = new File(this.FilePath+"/DataStore.json");
        file.createNewFile();
        readJson();
        
        fos = new FileOutputStream(file, true);
        if(! (fl==null))
        fl.release();
        fl = fos.getChannel().lock();
    }

    public void readJson()
    {
        try(FileReader fReader = new FileReader(file))
        {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(fReader); 
            // parsing the content of file
            jObject = (JSONObject)object; 
            // type casting contents to JSONObject
        }
        catch(ParseException pe) 
        // Exception caught when format of the file is not correct
        {
            jObject = new JSONObject(); 
        // an empty JSONObject is created
        }
        catch(IOException ie)
        {
            System.out.println("IOException");
        }

    }
    
   public int getFileSize() //Return size of the JSONObject in Bytes
   {
        byte[] values = jObject.toJSONString().getBytes();
        return values.length;
   }

   //create key when No expiry time is not given
   public synchronized String createKey(String key, String value)
           throws IOException, InterruptedException
    { 
        return createKey(key, value, -1);
    }

   /*create key when No expiry time is given. Negative expiry time indicates no
   expiry time*/
   public synchronized String createKey(String key, String value, 
           int expirytime)throws IOException
    {
        if(jObject.containsKey(key)) 
        {
            //System.out.println("Key already exists");
            return "Key already exists";
        }

        if(key.length()>32)
        {
            //System.out.println("Number of Characters in the key must not exceed 32");
            return "Number of Characters in the key must not exceed 32";
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long timeofexpiry = -1;

        /*finding time since epoch and adding expiry time to it so as to know 
        the time until when key would
        be valid*/
        if(expirytime > 0)
        {
        timeofexpiry = calendar.getTimeInMillis() + ((int)expirytime*1000);
        }

        JSONObject obj = new JSONObject(); 
        // json object to contail value and expiry time
        obj.put("value", value);
        byte[] values = obj.toJSONString().getBytes();
        
        //System.out.println(values.length);
        if(values.length > valuecap) // value capped (16KB) constraint on value
        {
            //System.out.println("Value size exceeds 16 KB. Failed to Insert value");
            return "Value size exceeds 16 KB. Failed to Insert value";
        } 
        obj.put("expirytime", timeofexpiry);
        jObject.put(key, obj);

        String str = jObject.toJSONString();
        values = str.getBytes();
      
        if(values.length > this.FileSize) // Max File Size (1 GB) constraint
        {
            jObject.remove(key);
            //System.out.println("Max File Size Exceeded");
            return "Max File Size Exceeded";
        }  

        //If all constraint pass, we are ready to write to json file
        fl.release();
        fos = new FileOutputStream(file);
        fl = fos.getChannel().lock();
        fos.write(values);
        //System.out.println("Insertion Success");
        return "Insertion Success";
    }

    //Function reads the value from file
    public synchronized JSONObject readKey(String key)
    {
        try{
            Thread.currentThread();
            Thread.sleep(1000);
        }
        catch(InterruptedException e)
        {
            System.out.println("Thread Interrupted");
        }

        JSONObject ret = new JSONObject();
        if(jObject.containsKey(key)) //checking if key exists
        {
            if(keyExpired(key)) //checking if key has expired
            {
                System.out.println("Key Expired");
                return ret;
            }
        ret.put(key, ((JSONObject)jObject.get(key)).get("value")); 
        /*returning key and value in JSON format*/
        }
        else
            System.out.println("Key does not exist");
        return ret;
    }

     public synchronized String deleteKey(String key)throws IOException
    {
        if(!jObject.containsKey(key)) //checking if key exists
        {
            //System.out.println("Key does not exist");
            return "Key does not exist";
        }
        if(keyExpired(key)) //checking if key has expired
            {
                //System.out.println("Key Expired");
                return "Key Expired";
            }
        
        /* Removing key and updating to the JSON file*/    
        jObject.remove(key);
        String str = jObject.toJSONString();
        byte[] values = str.getBytes();
        fl.release();
        fos = new FileOutputStream(file);
        fl = fos.getChannel().lock();
        fos.write(values);
        return "Deletion Successful";
    }

    /* Function check whether key has expired or not*/  
    public boolean keyExpired(String key)
    {
        JSONObject timeofexpiry = (JSONObject)jObject.get(key);
        long expirytime = (Long)timeofexpiry.get("expirytime");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currenttime = calendar.getTimeInMillis();
        return currenttime > expirytime && expirytime > 0;
    }
    
    public void releaseLock()throws IOException
    {
        fl.release();
    }
}