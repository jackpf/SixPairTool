package com.jackpf.sixpairtool;

import android.bluetooth.BluetoothAdapter;

public class BTAdapter
{
    public static String getMac()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (btAdapter != null) {
            return btAdapter.getAddress();
        } else {
            return null;
        }
    }
}
