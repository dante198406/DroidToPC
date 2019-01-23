package com.erobbing.adb_config_demo.sdk.utils;

import android.util.Log;

public abstract class StartableObject implements Startable {
    private boolean mStarted = false;

    public StartableObject() {

    }

    protected abstract void onStart();

    protected abstract void onStop();

    @Override
    public boolean isStarted() {
        return mStarted;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        if (mStarted)
            return;
        try {
            Log.i(getName(), "[ start ] startable: " + getName());
            onStart();
            mStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        if (mStarted == false)
            return;
        try {
            Log.w(getName(), "[ STOP ] startable: " + getName());
            onStop();
            mStarted = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void restart() {
        stop();
        start();

    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }


}
