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

/**
 * Sixaxis controller methods
 */
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
     * Logger
     */
    public final Logger logger;
    
    /**
     * Constructor
     * 
     * @param context
     */
    public SixPair(Context context)
    {
        manager     = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        permissions = new UsbPermissions();
        logger      = new Logger(context);
    }
    
    /**
     * Find device from intent
     * 
     * @param intent
     * @return
     */
    public UsbDevice findDevice(Intent intent)
    {
        logger.log("Attempting to find device from intent");
        
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        
        if (isController(device)) {
            return device;
        } else {
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
        logger.log("Searching all devices");
        
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        
        logger.log("Found %d devices", deviceList.size());
        
        for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
            UsbDevice device = entry.getValue();
            
            if (isController(device)) {
                return device;
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
     */
    public String bytesToMac(byte[] bytes)
    {
        if (bytes == null || bytes.length != 6) {
            return null;
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
     */
    public byte[] macToBytes(String mac)
    {
        StringTokenizer tok = new StringTokenizer(mac, ":");
        
        if (mac == null || tok.countTokens() != 6) {
            return null;
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
        logger.log("Reading master address...");
        
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
            logger.log("Unable to communicate with device");
            
            return null;
        }
        
        byte[] bytes = Utils.getBytes(buffer, 2, buffer.length);
        
        logger.log("Master: %s", bytesToMac(bytes));
        
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

        logger.log("Setting master address to %s", bytesToMac(bytes));
        
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
            logger.log("Unable to communicate with device");
            
            return false;
        }
        
        logger.log("Master set successfully");
        
        return true;
    }
    
    /**
     * Check device is a ps3 controller
     * 
     * @param device
     * @return
     */
    protected boolean isController(UsbDevice device)
    {
        //return device != null && device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID;
        
        if (device == null) {
            logger.log("No device found");
            
            return false;
        } else {
            logger.log(
                "Device  %s:\n\tvendorId: %s\n\tproductId: %s\n\tclass: %s",
                device.getDeviceName(),
                device.getVendorId(),
                device.getProductId(),
                device.getDeviceClass()
            );
            
            if (device.getVendorId() != VENDOR_ID || device.getProductId() != PRODUCT_ID) {
                logger.log("Warning: Device vendorId and productId do not match");
            }
            
            return true;
        }
    }
    
    /**
     * Opens a connection to a device
     * 
     * @param device
     * @return
     */
    protected UsbDeviceConnection openConnection(UsbDevice device)
    {
        UsbDeviceConnection connection = manager.openDevice(device); 
        connection.claimInterface(device.getInterface(0), true);
        
        return connection;
    }
}
