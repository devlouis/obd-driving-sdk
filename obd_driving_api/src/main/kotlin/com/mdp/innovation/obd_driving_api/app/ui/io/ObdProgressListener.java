package com.mdp.innovation.obd_driving_api.app.ui.io;

public interface ObdProgressListener {

    void stateUpdate(final ObdCommandJob job);

}