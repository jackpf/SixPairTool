package com.jackpf.sixpairtool;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class UsbPermissions
{
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    
    private Callback callback;
    
    public PendingIntent register(Context context, Callback callback)
    {
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(receiver, filter);
        
        this.callback = callback;
        
        return pi;
    }
    
    public static interface Callback
    {
        public void granted(UsbDevice device);
        public void denied(UsbDevice device);
    }
    
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            if (callback instanceof Callback) {
                                callback.granted(device);
                            }
                        }
                    } 
                    else {
                        if (callback instanceof Callback) {
                            callback.denied(device);
                        }
                    }
                }
            }
        }
    };
}
