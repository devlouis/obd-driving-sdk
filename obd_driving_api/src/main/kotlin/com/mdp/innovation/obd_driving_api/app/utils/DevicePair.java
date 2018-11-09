package com.mdp.innovation.obd_driving_api.app.utils;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

public class DevicePair {
    public void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
