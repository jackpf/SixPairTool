package com.jackpf.sixpairtool;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
    }
    
    public void manualPair(View v)
    {
        PairReceiver.startPairService(this, null);
    }
    
    public void sendLog()
    {
        PairReceiver.startPairService(this, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }
}
