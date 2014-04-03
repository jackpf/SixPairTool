package com.jackpf.sixpairtool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class SixPair
{
    /**
     * Controller info
     */
    private final int
        VENDOR_ID   = 1356,
        PRODUCT_ID  = 616
    ;
    
    /**
     * Usb manager
     */
    private UsbManager manager;
    
    /**
     * Permissions manager
     */
    private UsbPermissions permissions;
    
    /**
     * Constructor
     * 
     * @param context
     */
    public SixPair(Context context)
    {
        manager     = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        permissions = new UsbPermissions();
    }
    
    /**
     * Find device from intent
     * 
     * @param intent
     * @return
     */
    public UsbDevice findDevice(Intent intent)
    {
        log("Attempting to find device from intent");
        
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        
        if (isController(device)) {
            log("Controller found!");
            
            return device;
        } else {
            log("No or invalid device found");
            
            return null;
        }
    }
    
    /**
     * Find device by searching connected devices
     * 
     * @param context
     * @return
     */
    public UsbDevice findDevice(Context context)
    {
        log("Searching all devices");
        
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        
        for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
            UsbDevice device = entry.getValue();
            
            if (isController(device)) {
                log("Device %s found, valid controller", device.getDeviceName());
                
                return device;
            } else {
                log("Device %s found, not valid controller", device.getDeviceName());
            }
        }
        
        return null;
    }
    
    /**
     * Request permission to access device
     * 
     * @param context
     * @param device
     * @param callback
     */
    public void requestPermission(Context context, UsbDevice device, UsbPermissions.Callback callback)
    {
        manager.requestPermission(device, permissions.register(context, callback));
    }
    
    /**
     * Convert byte array to mac address
     * 
     * @param bytes
     * @return
     * @throws IOException
     */
    public String bytesToMac(byte[] bytes) throws IOException
    {
        if (bytes.length != 6) {
            throw new IOException(String.format("Invalid byte length: %d", bytes.length));
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            
            if (i < bytes.length - 1) {
                sb.append(":");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Convert mac address to byte array
     * 
     * @param mac
     * @return
     * @throws IOException
     */
    public byte[] macToBytes(String mac) throws IOException
    {
        StringTokenizer tok = new StringTokenizer(mac, ":");
        
        if (tok.countTokens() != 6) {
            throw new IOException(String.format("Invalid mac length: %d", tok.countTokens()));
        }
        
        byte[] bytes = new byte[6];
        
        for (int i = 0; tok.hasMoreElements(); i++) {
            bytes[i] = (byte) Integer.parseInt(tok.nextElement().toString(), 16);
        }
        
        return bytes;
    }
    
    /**
     * Get master mac of device
     * 
     * @param device
     * @return
     * @throws IOException
     */
    public byte[] getMaster(UsbDevice device) throws IOException
    {
        log("Reading master address...");
        
        byte[] buffer = new byte[8];

        int r = openConnection(device).controlTransfer(
            0xa1,           // Request type
            0x01,           // Request ID
            0x03f5,         // Value
            0,              // Interface ID
            buffer,         // Buffer
            buffer.length,  // Buffer length
            1000
        );
        
        if (r < 1) {
            log("Unable to communicate with device");
            
            return null;
        }
        
        byte[] bytes = Utils.getBytes(buffer, 2, buffer.length);
        
        log("Master: %s", bytesToMac(bytes));
        
        return bytes;
    }
    
    /**
     * Set master mac of device
     * 
     * @param device
     * @param bytes
     * @return
     * @throws IOException
     */
    public boolean setMaster(UsbDevice device, byte[] bytes) throws IOException
    {
        if (bytes.length != 6) {
            throw new IOException("Invalid mac address");
        }

        log("Setting master address to %s", bytesToMac(bytes));
        
        byte[] buffer = new byte[8];
        Utils.setBytes(buffer, 0, new byte[]{0x01, 0x00});
        Utils.setBytes(buffer, 2, bytes);
        
        int r = openConnection(device).controlTransfer(
            0x21,           // Request type
            0x09,           // Request ID
            0x03f5,         // Value
            0,              // Interface ID
            buffer,         // Buffer
            buffer.length,  // Buffer length
            1000
        );
        
        if (r < 1) {
            log("Unable to communicate with device");
            
            return false;
        }
        
        log("Master set successfully");
        
        return true;
    }
    
    /**
     * Print device info
     * 
     * @param device
     */
    public void printDeviceInfo(UsbDevice device)
    {
        log(
            "Device  %s:\nvendorId: %s\nproductId: %s\nclass: %s",
            device.getDeviceName(),
            device.getVendorId(),
            device.getProductId(),
            device.getDeviceClass()
        );
    }
    
    /**
     * Log
     * 
     * @param s
     * @param p
     */
    public void log(String s, Object ...p)
    {
        Log.d(SixPair.class.getName(), String.format(s, p));
    }
    
    /**
     * Check device is a ps3 controller
     * 
     * @param device
     * @return
     */
    protected boolean isController(UsbDevice device)
    {
        return device != null && device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID;
    }
    
    protected UsbDeviceConnection openConnection(UsbDevice device)
    {
        UsbDeviceConnection connection = manager.openDevice(device); 
        connection.claimInterface(device.getInterface(0), true);
        
        return connection;
    }
}