package com.mdp.innovation.obd_driving_api.app.utils;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

public class DevicePair {

    /**
     * Emparejar dispositivo
     */
    public void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Desemparejar dispositivo
     */
    public void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
