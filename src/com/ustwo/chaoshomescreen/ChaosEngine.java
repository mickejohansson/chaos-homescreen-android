package com.ustwo.chaoshomescreen;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class ChaosEngine extends Thread implements Callback {

    private boolean mIsRunning = false;
    private SurfaceHolder mHolder;
    private World mWorld;
    private Drawable mWallpaperDrawable;
    private int mScreenWidth;
    private int mScreenHeight;
    private ArrayList<ChaosIcon> mChaosIcons;
    
    private int metersToPx(float worldMeters) {
       return (int)(worldMeters * 10);
    }
    
    private float pxToMeters(int pixels) {
        return pixels / 10.0f;
    }

    public ChaosEngine(SurfaceHolder holder, Context context) {
        mHolder = holder;
        mHolder.addCallback(this);
      
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
  
        // Setup the world
        Vec2 gravity = new Vec2(0.0f, 9.82f);
        mWorld = new World(gravity);
        //mWorld.setAllowSleep(false);
      
        // The ground
        BodyDef staticDef = new BodyDef();
        staticDef.setPosition(new Vec2(pxToMeters(mScreenWidth / 2), pxToMeters(mScreenHeight) + 5.0f));
        Body groundBody = mWorld.createBody(staticDef);
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(pxToMeters(mScreenWidth/2), 5.0f);
        groundBody.createFixture(groundShape, 0.0f);
      
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(intent, 0);
       
        BodyDef iconBodyDef = new BodyDef();
        iconBodyDef.setType(BodyType.DYNAMIC);
        PolygonShape iconShape = new PolygonShape();
        iconShape.setAsBox(pxToMeters(ChaosIcon.ICON_SIZE / 2), pxToMeters(ChaosIcon.ICON_SIZE / 2));
        FixtureDef iconFixtureDef = new FixtureDef();
        iconFixtureDef.setShape(iconShape);
        iconFixtureDef.setDensity(10.0f);
        iconFixtureDef.setFriction(0.1f);
        iconFixtureDef.setRestitution(0.2f);

        mChaosIcons = new ArrayList<ChaosIcon>();
        for (int i=0; i<appList.size(); i++) {
            ResolveInfo tempInfo = appList.get(i);
            iconBodyDef.setPosition(new Vec2(pxToMeters(mScreenWidth / 2), 0.0f - i*pxToMeters(ChaosIcon.ICON_SIZE)));
            Body body = mWorld.createBody(iconBodyDef);
            body.createFixture(iconFixtureDef);
            ChaosIcon tempIcon = new ChaosIcon(body, tempInfo.loadIcon(context.getPackageManager()));
            
            mChaosIcons.add(tempIcon);
            i++;
        }
        
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        mWallpaperDrawable = wallpaperManager.getDrawable();
    }
        
    @Override
    public void run() {
        long millisNow;
        long millisPrev = System.currentTimeMillis();
        int posX, posY;
        while (mIsRunning) {
            millisNow = System.currentTimeMillis();
            mWorld.step((millisNow - millisPrev) / 1000.0f, 8, 3);
            mWorld.clearForces();
          
            Canvas canvas = mHolder.lockCanvas();
            canvas.drawColor(Color.DKGRAY);
            //mWallpaperDrawable.setBounds(0, 0, mScreenWidth, mScreenHeight);
            //mWallpaperDrawable.draw(canvas);
            for (ChaosIcon icon : mChaosIcons) {
                posX = metersToPx(icon.getBody().getPosition().x);
                posY = metersToPx(icon.getBody().getPosition().y);
                canvas.save();
                icon.getDrawable().setBounds(posX - ChaosIcon.ICON_SIZE/2, 
                                             posY - ChaosIcon.ICON_SIZE/2, 
                                             posX + ChaosIcon.ICON_SIZE/2, 
                                             posY + ChaosIcon.ICON_SIZE/2);
                
                canvas.rotate((float)(icon.getBody().getAngle() * 180.0f / Math.PI), 
                              metersToPx(icon.getBody().getPosition().x), 
                              metersToPx(icon.getBody().getPosition().y));
                icon.getDrawable().draw(canvas);
                canvas.restore();
            }
            /*
            System.out.println("fps = " + 1000.0f / (millisNow - millisPrev));
            System.out.println("iconBody.x = " + mIconBody.getPosition().x);
            System.out.println("iconBody.y = " + mIconBody.getPosition().y);
            System.out.println("iconBody2.angle = " + mIconBody2.getAngle());
            */
            mHolder.unlockCanvasAndPost(canvas);
            
            millisPrev = millisNow;
        }
    }
   
    // Will be called on the UI thread, watch out!
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    // Will be called on the UI thread, watch out!
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsRunning = true;
        start();
    }

    // Will be called on the UI thread, watch out!
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRunning = false;
    }
}
