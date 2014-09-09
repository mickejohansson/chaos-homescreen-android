package com.ustwo.chaoshomescreen;

import java.util.List;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set background image
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        RelativeLayout bgLayout = (RelativeLayout)findViewById(R.id.bg_layout);
        bgLayout.setBackground(wallpaperDrawable);
        
        // Get all the launcher apps
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(intent, 0);
        
        // Display the launcher apps
        int nbrApps = 0;
        for (ResolveInfo app:appList) {
        	ImageView imageView = new ImageView(getApplicationContext());
        	Drawable icon = app.activityInfo.loadIcon(packageManager);
        	imageView.setImageDrawable(icon);
        	int iconSize = (int)getResources().getDisplayMetrics().density * 48;
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iconSize, iconSize);
        	params.leftMargin = iconSize * (nbrApps % 12);
        	params.topMargin = iconSize * (int)(nbrApps / 12);
        	imageView.setLayoutParams(params);
        	bgLayout.addView(imageView);
        	nbrApps++;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
