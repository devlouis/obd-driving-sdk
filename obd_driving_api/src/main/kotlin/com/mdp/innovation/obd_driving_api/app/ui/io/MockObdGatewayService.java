package com.mdp.innovation.obd_driving_api.app.ui.io;

import android.util.Log;
import android.widget.Toast;
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity;
import com.mdp.innovation.obd_driving_api.commands.protocol.*;
import com.mdp.innovation.obd_driving_api.commands.temperature.AmbientAirTemperatureCommand;
import com.mdp.innovation.obd_driving_api.enums.ObdProtocols;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * This service is primarily responsible for establishing and maintaining a
 * permanent connection between the device where the application runs and a more
 * OBD Bluetooth interface.
 * <p/>
 * Secondarily, it will serve as a repository of ObdCommandJobs and at the same
 * time the application state-machine.
 */
public class MockObdGatewayService extends AbstractGatewayService {

    private static final String TAG = MockObdGatewayService.class.getName();

    public void startService() {
        Log.d(TAG, "Starting " + this.getClass().getName() + " service..");

        // Let's configure the connection.
        Log.d(TAG, "Queing jobs for connection configuration..");
        queueJob(new ObdCommandJob(new ObdResetCommand()));
        queueJob(new ObdCommandJob(new EchoOffCommand()));

    /*
     * Will send second-time based on tests.
     *
     * TODO this can be done w/o having to queue jobs by just issuing
     * command.run(), command.getResult() and validate the result.
     */
        queueJob(new ObdCommandJob(new EchoOffCommand()));
        queueJob(new ObdCommandJob(new LineFeedOffCommand()));
        queueJob(new ObdCommandJob(new TimeoutCommand(62)));

        // For now set protocol to AUTO
        queueJob(new ObdCommandJob(new SelectProtocolCommand(ObdProtocols.AUTO)));

        // Job for returning dummy data
        queueJob(new ObdCommandJob(new AmbientAirTemperatureCommand()));

        queueCounter = 0L;
        Log.d(TAG, "Initialization jobs queued.");

        isRunning = true;
    }


    /**
     * Runs the queue until the service is stopped
     */
    protected void executeQueue() {
        Log.d(TAG, "Executing queue..");
        while (!Thread.currentThread().isInterrupted()) {
            ObdCommandJob job = null;
            try {
                job = jobsQueue.take();

                Log.d(TAG, "Taking job[" + job.getId() + "] from queue._.");
                Toast.makeText(this, " _Taking_", Toast.LENGTH_LONG).show();
                if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NEW)) {
                    Log.d(TAG, "Job state is NEW. Run it._.");
                    job.setState(ObdCommandJob.ObdCommandJobState.RUNNING);
                    Log.d(TAG, " GetName:: " + job.getCommand().getName());
                    job.getCommand().run(new ByteArrayInputStream("41 00 00 00>41 00 00 00>41 00 00 00>".getBytes()), new ByteArrayOutputStream());
                } else {
                    Log.e(TAG, "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
                }
            } catch (InterruptedException i) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                if (job != null) {
                    job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                }
                Log.e(TAG, "Failed to run command. -> " + e.getMessage());
            }

            if (job != null) {
                Log.d(TAG, "Job is finished.");
                job.setState(ObdCommandJob.ObdCommandJobState.FINISHED);
                final ObdCommandJob job2 = job;
                ((PairObdActivity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((PairObdActivity) ctx).stateUpdate(job2);
                    }
                });

            }
        }
    }


    /**
     * Stop OBD connection and queue processing.
     */
    public void stopService() {
        Log.d(TAG, "Stopping service..");

        notificationManager.cancel(NOTIFICATION_ID);
        jobsQueue.clear();
        isRunning = false;

        // kill service
        stopSelf();
    }

}
