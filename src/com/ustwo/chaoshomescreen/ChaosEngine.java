package com.ustwo.chaoshomescreen;

import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class ChaosEngine extends Thread implements Callback {

    private boolean mIsRunning = false;
    private SurfaceHolder mHolder;
    private Body mIconBody;
    private Body mIconBody2;
    private World mWorld;
    private Drawable mIconDrawable;
    private Drawable mIconDrawable2;
    
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
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
  
        // Setup the world
        Vec2 gravity = new Vec2(0.0f, 9.82f);
        mWorld = new World(gravity);
        mWorld.setAllowSleep(false);
      
        // The ground
        BodyDef staticDef = new BodyDef();
        staticDef.setPosition(new Vec2(pxToMeters(screenWidth / 2), pxToMeters(screenHeight) + 5.0f));
        Body groundBody = mWorld.createBody(staticDef);
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(pxToMeters(screenWidth/2), 5.0f);
        groundBody.createFixture(groundShape, 0.0f);
       
        // The icon 
        BodyDef iconBodyDef = new BodyDef();
        iconBodyDef.setPosition(new Vec2(pxToMeters(screenWidth / 2), 0.0f));
        iconBodyDef.setType(BodyType.DYNAMIC);
        mIconBody = mWorld.createBody(iconBodyDef);
        PolygonShape iconShape = new PolygonShape();
        iconShape.setAsBox(pxToMeters(120/2), pxToMeters(120/2));
        FixtureDef iconFixtureDef = new FixtureDef();
        iconFixtureDef.setShape(iconShape);
        iconFixtureDef.setDensity(10.0f);
        iconFixtureDef.setFriction(0.1f);
        iconFixtureDef.setRestitution(0.2f);
        mIconBody.createFixture(iconFixtureDef);
        
        // Another icon
        iconBodyDef.setPosition(new Vec2(pxToMeters(screenWidth / 2 - 45), -20.0f));
        iconBodyDef.setType(BodyType.DYNAMIC);
        mIconBody2 = mWorld.createBody(iconBodyDef);
        mIconBody2.createFixture(iconFixtureDef);
       
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(intent, 0);
        mIconDrawable = appList.get(0).loadIcon(context.getPackageManager());
        mIconDrawable2 = appList.get(1).loadIcon(context.getPackageManager());
    }
        
    @Override
    public void run() {
        long millisNow;
        long millisPrev = System.currentTimeMillis();
        while (mIsRunning) {
            millisNow = System.currentTimeMillis();
            mWorld.step((millisNow - millisPrev) / 1000.0f, 8, 3);
            mWorld.clearForces();
            Canvas canvas = mHolder.lockCanvas();
            canvas.drawColor(Color.DKGRAY);
            mIconDrawable.setBounds(metersToPx(mIconBody.getPosition().x) - 60, metersToPx(mIconBody.getPosition().y) - 60, metersToPx(mIconBody.getPosition().x) + 60, metersToPx(mIconBody.getPosition().y) + 60);
            mIconDrawable.draw(canvas);
            mIconDrawable2.setBounds(metersToPx(mIconBody2.getPosition().x) - 60, metersToPx(mIconBody2.getPosition().y) - 60, metersToPx(mIconBody2.getPosition().x) + 60, metersToPx(mIconBody2.getPosition().y) + 60);
            mIconDrawable2.draw(canvas);
            /*
            System.out.println("fps = " + 1000.0f / (millisNow - millisPrev));
            System.out.println("iconBody.x = " + mIconBody.getPosition().x);
            */
            System.out.println("iconBody.y = " + mIconBody.getPosition().y);
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
