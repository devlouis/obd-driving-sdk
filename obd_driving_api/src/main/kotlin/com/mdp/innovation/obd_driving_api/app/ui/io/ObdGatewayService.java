package com.mdp.innovation.obd_driving_api.app.ui.io;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import com.mdp.innovation.obd_driving_api.R;
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD;
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity;
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils;
import com.mdp.innovation.obd_driving_api.commands.protocol.*;
import com.mdp.innovation.obd_driving_api.commands.temperature.AmbientAirTemperatureCommand;
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference;
import com.mdp.innovation.obd_driving_api.enums.ObdProtocols;
import com.mdp.innovation.obd_driving_api.exceptions.UnsupportedCommandException;



import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames.VIN;

/**
 * This service is primarily responsible for establishing and maintaining a
 * permanent connection between the device where the application runs and a more
 * OBD Bluetooth interface.
 * <p/>
 * Secondarily, it will serve as a repository of ObdCommandJobs and at the same
 * time the application state-machine.
 */
public class ObdGatewayService extends AbstractGatewayService  {

    private static final String TAG = ObdGatewayService.class.getName();
    /*@Inject
    SharedPreferences prefs;*/

    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;

    Boolean IMPERIAL_UNITS_KEY = false;
    String PROTOCOLS_LIST_KEY = "AUTO";
    SharedPreference appSharedPreference;

    Activity activity;

    Context context;

    /*public ObdGatewayService(Context context) {
        this.context = context;
    }*/

    /**
     * Sensores
     */
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorGyr;
    private Sensor mSensorMgt;
    private SensorEventListener sensorEventListener;

    private SensorEvent sensorAccelerometer;
    private SensorEvent sensorGyroscope;
    private SensorEvent sensorMagneticr;

    public void startService() throws IOException {
        Log.d(TAG, "Starting service..");
        Log.v(" OBDRestar ", " Starting service...*");
        this.activity = activity;
        initSensor();
        // get the remote Bluetooth device
        //final String remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        appSharedPreference = new SharedPreference(this);
        HashMap<String, String> Macdevice = appSharedPreference.getMacBluetooth();
        final String remoteDevice = Macdevice.get(appSharedPreference.getMAC_DEVICE());
        Log.v(" OBDRestar ", " remoteDevice " + remoteDevice);
        Log.v(" remoteDevice: ", remoteDevice);

        if (remoteDevice == null || "".equals(remoteDevice)) {
            Toast.makeText(ctx, getString(R.string.text_bluetooth_nodevice), Toast.LENGTH_LONG).show();

            // log error
            Log.e(TAG, "No Bluetooth device has been selected.");

            // TODO kill this service gracefully
            stopService();
            throw new IOException();
        } else {

            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            dev = btAdapter.getRemoteDevice(remoteDevice);


    /**
     * Establish Bluetooth connection
     *
     * Because discovery is a heavyweight procedure for the Bluetooth adapter,
     * this method should always be called before attempting to connect to a
     * remote device with connect(). Discovery is not managed by the Activity,
     * but is run as a system service, so an application should always call
     * cancel discovery even if it did not directly request a discovery, just to
     * be sure. If Bluetooth state is not STATE_ON, this API will return false.
     *
     * see
     * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
     * .html#cancelDiscovery()
     */
            Log.d(TAG, "Stopping Bluetooth discovery.");
            btAdapter.cancelDiscovery();

            //showNotification(getString(R.string.notification_action), getString(R.string.service_starting), R.drawable.ic_bluetooth, true, true, false);

            try {
                startObdConnection();
            } catch (Exception e) {
                Log.e(
                        TAG,
                        "There was an error while establishing connection. -> "
                                + e.getMessage()
                );

                // in case of failure, stop this service.
                stopService();
                throw new IOException();
            }
            //showNotification(getString(R.string.notification_action), getString(R.string.service_started), R.drawable.ic_bluetooth, true, true, false);
        }
    }

    /**
     * Start and configure the connection to the OBD interface.
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @throws IOException
     */
    private void startObdConnection() throws IOException {
        Log.d(TAG, "Starting OBD connection..");
        Log.v(" OBDRestar ", " Starting OBD connection.. ");
        isRunning = true;
        try {
            sock = BluetoothManager.connect(dev);
            startSensor();
        } catch (Exception e2) {
            Log.e(TAG, "There was an error while establishing Bluetooth connection. Stopping app..", e2);
            stopService();
            throw new IOException("eoeoeoeoe");
        }

        // Let's configure the connection.
        Log.d(TAG, "Queueing jobs for connection configuration..");
        queueJob(new ObdCommandJob(new ObdResetCommand()));
        
        //Below is to give the adapter enough time to reset before sending the commands, otherwise the first startup commands could be ignored.
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        
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

        // Get protocol from preferences
        //final String protocol = prefs.getString(ConfigActivity.PROTOCOLS_LIST_KEY, "AUTO");
        final String protocol = PROTOCOLS_LIST_KEY;
        queueJob(new ObdCommandJob(new SelectProtocolCommand(ObdProtocols.valueOf(protocol))));

        // Job for returning dummy data
        queueJob(new ObdCommandJob(new AmbientAirTemperatureCommand()));

        queueCounter = 0L;
        Log.d(TAG, "Initialization jobs queued.");


    }

    /**
     * This method will add a job to the queue while setting its ID to the
     * internal queue counter.
     *
     * @param job the job to queue.
     */
    @Override
    public void queueJob(ObdCommandJob job) {
        // This is a good place to enforce the imperial units option
        //job.getCommand().useImperialUnits(prefs.getBoolean(ConfigActivity.IMPERIAL_UNITS_KEY, false));
        job.getCommand().useImperialUnits(IMPERIAL_UNITS_KEY);

        // Now we can pass it along
        super.queueJob(job);
    }

    /**
     * Runs the queue until the service is stopped
     */
    protected void executeQueue() throws InterruptedException {
        Log.d(TAG, "Executing queue..");

        while (!Thread.currentThread().isInterrupted()) {
            ObdCommandJob job = null;
            try {
                job = jobsQueue.take();

                // log job
                Log.d(TAG, "Taking job[" + job.getId() + "] from queue._.");

                if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NEW)) {
                    Log.d(TAG, "Job state is NEW. Run it..");
                    job.setState(ObdCommandJob.ObdCommandJobState.RUNNING);
                    if (sock.isConnected()) {
                        Log.d(TAG, " GetCommand:: " + job.getCommand().getFormattedResult());
                        job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
                    } else {
                        job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                        Log.e(TAG, "Can't run command on a closed socket.");
                    }
                } else
                    // log not new job
                    Log.e(TAG,
                            "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
            } catch (InterruptedException i) {
                Thread.currentThread().interrupt();
            } catch (UnsupportedCommandException u) {
                if (job != null) {
                    job.setState(ObdCommandJob.ObdCommandJobState.NOT_SUPPORTED);
                }
                Log.d(TAG, "Command not supported. -> " + u.getMessage());
            } catch (IOException io) {
                if (job != null) {
                    if(io.getMessage().contains("Broken pipe"))
                        job.setState(ObdCommandJob.ObdCommandJobState.BROKEN_PIPE);
                    else
                        job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                }
                Log.e(TAG, "IO error. -> " + io.getMessage());
            } catch (Exception e) {
                if (job != null) {
                    job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                }
                Log.e(TAG, "Failed to run command. -> " + e.getMessage());
            }

            if (job != null) {
            /*    SendDataOBD send = new SendDataOBD();
                try {
                    send.InitClient();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                send.sendData2();*/
                final ObdCommandJob job2 = job;
                ConnectOBD.stateUpdate(job2, sensorAccelerometer ,ctx);
            }
        }

    }

    /**
     * Stop OBD connection and queue processing.
     */
    public void stopService() {
        Log.d(TAG, "Stopping service..");
        stopSensor();
        //notificationManager.cancel(NOTIFICATION_ID);
        jobsQueue.clear();
        isRunning = false;

        if (sock != null)
            // close socket
            try {
                sock.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

        // kill service
        stopSelf();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void saveLogcatToFile(Context context, String devemail) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{devemail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OBD2 Reader Debug Logs");

        StringBuilder sb = new StringBuilder();
        sb.append("\nManufacturer: ").append(Build.MANUFACTURER);
        sb.append("\nModel: ").append(Build.MODEL);
        sb.append("\nRelease: ").append(Build.VERSION.RELEASE);

        emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        String fileName = "OBDReader_logcat_" + System.currentTimeMillis() + ".txt";
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + File.separator + "OBD2Logs");
        if (dir.mkdirs()) {
            File outputFile = new File(dir, fileName);
            Uri uri = Uri.fromFile(outputFile);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

            Log.d("savingFile", "Going to save logcat to " + outputFile);
            //emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            try {
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

/*    @Override
    public void onDestroy() {
        Log.v(" OBDRestar ", "broadcast 4");
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, OBDRestarBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stopService();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        ConnectOBD.startmQueue();
        return START_STICKY;
    }*/

    /**
     * OBTENER SENSORES
     */
    public void initSensor(){
        /**
         * obtener sensores para usar
         */
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorMgt = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        /**
         * validar si existe sensor
         */
        if (mSensorAcc == null)
            new LogUtils().v(TAG, " No se encuentra sensor de Acelerometro" );
        if (mSensorGyr == null)
            new LogUtils().v(TAG, " No se encuentra sensor de Giroscopio" );
        if (mSensorMgt == null)
            new LogUtils().v(TAG, " No se encuentra sensor de Magnetometro" );

        getSensor();
    }

    private void getSensor(){
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()){
                    case Sensor.TYPE_ACCELEROMETER :
                        sensorAccelerometer = event;
                        new LogUtils().v("SENSOR ACCE", "X =" + sensorAccelerometer.values[0]);
                        new LogUtils().v("SENSOR ACCE", "Y =" + sensorAccelerometer.values[1]);
                        new LogUtils().v("SENSOR ACCE", "Z =" + sensorAccelerometer.values[2]);

                        break;
                    case Sensor.TYPE_GYROSCOPE :

                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD :

                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

    private void startSensor(){
        mSensorManager.registerListener(sensorEventListener, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorEventListener, mSensorGyr, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorEventListener, mSensorMgt, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void stopSensor(){
        mSensorManager.unregisterListener(sensorEventListener, mSensorAcc);
        mSensorManager.unregisterListener(sensorEventListener, mSensorGyr);
        mSensorManager.unregisterListener(sensorEventListener, mSensorMgt);
    }
}
