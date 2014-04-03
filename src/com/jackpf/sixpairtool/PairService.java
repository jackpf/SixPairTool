package com.jackpf.sixpairtool;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.widget.Toast;

public class PairService extends Service
{
    private SixPair sixPair;
    
    @Override
    public void onCreate()
    {
        sixPair = new SixPair(this);
    }
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
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
    
    protected void processDevice(UsbDevice device)
    {
        sixPair.requestPermission(this, device, new UsbPermissions.Callback() {
            public void granted(UsbDevice device) {
                sixPair.log("Permission granted");
                
                try {
                    sixPair.log("mac: " + sixPair.bytesToMac(sixPair.getMaster(device)));
                    
                    String btAddr = BTAdapter.getMac();
                    
                    if (btAddr == null) {
                        sixPair.log("Unable to obtain bluetooth address");
                    } else {
                        sixPair.log("BT addr: %s", btAddr);
                    }
                    
                    if (sixPair.setMaster(device, sixPair.macToBytes(btAddr))) {
                        Toast.makeText(getApplicationContext(), getString(R.string.pair_success), Toast.LENGTH_SHORT).show();
                    }
                    
                    sixPair.log("Current master: " + sixPair.bytesToMac(sixPair.getMaster(device)));
                } catch (IOException e) {
                    sixPair.log("IOException: %s", e.getMessage());
                    
                    Toast.makeText(getApplicationContext(), getString(R.string.pair_fail, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
            
            public void denied(UsbDevice device) {
                sixPair.log("Permission denied");
            }
        });
    }
}
