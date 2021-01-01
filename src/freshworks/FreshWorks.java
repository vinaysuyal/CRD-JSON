/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package freshworks;

import java.io.IOException;

/**
 *
 * @author ACER
 */
public class FreshWorks {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)throws IOException, InterruptedException {
        //DataStore ds = new DataStore();
        DataStore ds = new DataStore("D:/xyz");
        String keys[] = {"Book","God","Player","Heater","Drunkard"};
        String values[] = {"Kitab","Bhagwan","Khailadi","Heater","Nashedi"};
        for(int i = 0;i < keys.length;i++)
        {
            System.out.println(ds.createKey(keys[i], values[i]));
        }
        Thread.sleep(1000);
        //System.out.println(ds.deleteKey("Heater"));
        //System.out.println(ds.deleteKey("Drunkard"));
        for(int i = 0;i < 2;i++)
        {
            System.out.println(ds.deleteKey(keys[i]));
            Thread.sleep(1000);
        }
        
        for (String key : keys) {
            System.out.println(ds.readKey(key));
        }
        
    }
    
}
