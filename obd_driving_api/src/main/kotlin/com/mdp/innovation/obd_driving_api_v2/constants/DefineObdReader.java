package com.mdp.innovation.obd_driving_api_v2.constants;

import org.jetbrains.annotations.NotNull;

/**
 * Created by EMP203 on 5/17/2017.
 * <p>
 * provides constant used in Application
 */

public interface DefineObdReader {

    String ACTION_OBD_CONNECTION_STATUS = "ACTION_OBD_CONNECTION_STATUS";
    /**
     * Real-time data
     */
    String ACTION_READ_OBD_REAL_TIME_DATA = "com.sohrab.obd.reader.ACTION_READ_OBD_REAL_TIME_DATA";
    // intent key used to send data
     String INTENT_OBD_EXTRA_DATA = "com.sohrab.obd.reader.INTENT_OBD_EXTRA_DATA";


    String TAG_BD = "BD_LOCAL";
}
