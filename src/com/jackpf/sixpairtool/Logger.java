package com.jackpf.sixpairtool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

/**
 * Logger
 */
public class Logger
{
    /**
     * Log filename
     */
    protected static final String FILENAME = "sixpairtool.log";
    
    /**
     * Application context
     */
    protected Context context;
    
    /**
     * Log output stream
     */
    protected FileOutputStream log;
    
    /**
     * Constructor
     * Opens log output stream
     * 
     * @param context
     */
    public Logger(Context context)
    {
        this.context = context;
        
        try {
            log = context.openFileOutput(FILENAME, Context.MODE_PRIVATE | Context.MODE_APPEND);
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }
    
    /**
     * Log message
     * 
     * @param s
     * @param p
     */
    public void log(String s, Object ...p)
    {
        try {
            String m = String.format(s, p);
            
            Log.d(context.getString(context.getApplicationInfo().labelRes), m);
            
            log.write((m + System.getProperty("line.separator")).getBytes());
            log.flush();
        } catch(IOException e) { e.printStackTrace(); }
    }
    
    /**
     * Get log contents
     * 
     * @return
     */
    public String getLog()
    {
        try {
            FileInputStream reader = context.openFileInput(FILENAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(reader));
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
            
            return sb.toString();
        } catch (FileNotFoundException e) { e.printStackTrace(); } catch(IOException e) { e.printStackTrace(); }
        
        return null;
    }
}
