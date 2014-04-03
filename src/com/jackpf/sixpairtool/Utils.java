package com.jackpf.sixpairtool;

public class Utils 
{
    /**
     * Synonymous to Arrays.copyOfRange but rewritten for API level
     * 
     * @param array
     * @param start
     * @param end
     * @return
     */
    public static byte[] getBytes(byte[] array, int start, int end)
    {
        int range = end - start;
        byte[] copy = new byte[range];
        
        for (int i = start, j = 0; i < end; i++, j++) {
            copy[j] = array[i];
        }
        
        return copy;
    }
    
    public static byte[] setBytes(byte[] array, int start, byte[] values)
    {
        for (int i = start, j = 0; j < values.length; j++, i++) {
            array[i] = values[j];
        }
        
        return array;
    }
}
