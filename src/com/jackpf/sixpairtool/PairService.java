package com.jackpf.sixpairtool;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Pair service
 */
public class PairService extends Service
{
    /**
     * Sixpair instance
     */
    private SixPair sixPair;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate()
    {
        sixPair = new SixPair(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        UsbDevice device = sixPair.findDevice(this);
        
        if (device != null) {
            sixPair.printDeviceInfo(device);
            
            processDevice(device);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
        }
        
        return 0;
    }
    
    /**
     * Set controller's master address to the devices bluetooth address
     * 
     * @param device
     */
    protected void processDevice(UsbDevice device)
    {
        sixPair.requestPermission(this, device, new UsbPermissions.Callback() {
            public void granted(UsbDevice device) {
                sixPair.logger.log("Permission granted");
                
                try {
                    String btAddr = BTAdapter.getMac();
                    
                    if (btAddr == null) {
                        sixPair.logger.log("Unable to obtain bluetooth address");
                    } else {
                        sixPair.logger.log("BT addr: %s", btAddr);
                    }
                    
                    byte[] btMac = sixPair.macToBytes(btAddr);
                    
                    if (sixPair.setMaster(device, btMac)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.pair_success), Toast.LENGTH_SHORT).show();
                    }
                    
                    sixPair.logger.log("Current master: " + sixPair.bytesToMac(sixPair.getMaster(device)));
                } catch (IOException e) {
                    sixPair.logger.log("IOException: %s", e.getMessage());
                    
                    Toast.makeText(getApplicationContext(), getString(R.string.pair_fail, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
            
            public void denied(UsbDevice device) {
                sixPair.logger.log("Permission denied");
            }
        });
    }
}
