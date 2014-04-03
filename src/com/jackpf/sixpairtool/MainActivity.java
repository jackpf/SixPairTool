package com.jackpf.sixpairtool;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
    
    public void sendLog(View v)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        
        String uri = "mailto:" + Uri.encode(getString(R.string.log_email)) + 
                "?subject=" + Uri.encode(getString(R.string.log_subject)) + 
                "&body=" + new Logger(this).getLog();
        
        intent.setData(Uri.parse(uri));

        startActivity(Intent.createChooser(intent, "Send Log"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }
}
