package com.jackpf.sixpairtool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class PairReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        startPairService(context, (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE));
    }
    
    public static void startPairService(Context context, UsbDevice device)
    {
        Intent intent = new Intent(context, PairService.class);
        intent.putExtra(UsbManager.EXTRA_DEVICE, device);
        context.startService(intent);
    }
}
