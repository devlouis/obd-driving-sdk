package com.mdp.innovation.obd_driving_api.data.IoTHub;

import android.app.Application;
import android.content.Context;
import com.mdp.innovation.obd_driving_api.app.core.UtilsNetwork;
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils;
import com.mdp.innovation.obd_driving_api.app.utils.UtilsLocationService;
import com.mdp.innovation.obd_driving_api.data.entity.FailuresEntity;
import com.mdp.innovation.obd_driving_api.data.entity.FailuresTripValuesEntity;
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference;
import com.mdp.innovation.obd_driving_api.data.store.repository.FailuresTripValuesRepository;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendDataIoTHub {

    /*private final String connString = "HostName=digitalCar.azure-devices.net;DeviceId=prueba;SharedAccessKey=nPh5ND6bMSw20CQwb3PfnCfL4hVP+2dcC6/KLrlGqU0=";*/
   // private final String connString = "HostName=DCP-test.azure-devices.net;DeviceId=lois-android;SharedAccessKey=Q37BFPZONbrYKFsaxkpF1nLsgXERPdc6/T+QNhC/HIE=";
    private final String deviceId = "MyAndroidDevice";
    private DeviceClient client;
    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;

    private static int posTrip = 0;

    SharedPreference appSharedPreference;

    private static int method_command(Object command)
    {
        System.out.println("invoking command on this device");
        // Insert code to invoke command here
        return METHOD_SUCCESS;
    }

    private static int method_default(Object data)
    {
        System.out.println("invoking default method for this device");
        // Insert device specific code here
        return METHOD_NOT_DEFINED;
    }

    protected static class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData ;
            switch (methodName)
            {
                case "command" :
                {
                    int status = method_command(methodData);

                    deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                    break;
                }
                default:
                {
                    int status = method_default(methodData);
                    deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                }
            }

            return deviceMethodData;
        }
    }

    private static AtomicBoolean Succeed = new AtomicBoolean(false);

    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode status, Object context)
        {
            if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
            {
                Succeed.set(true);
            }
            else
            {
                Succeed.set(false);
            }
            System.out.println("IoT Hub responded to device twin operation with status " + status.name());
        }
    }

    protected static class onProperty implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            System.out.println(
                    "onProperty callback for " + (property.getIsReported()?"reported": "desired") +
                            " property " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }

    public void InitClient(String connString) throws URISyntaxException, IOException
    {

        //client = new DeviceClient(connString, protocol);

       /* HashMap<String, String> mapConnString = appSharedPreference.getConnStringIoTHub();
        final String connString = mapConnString.get(appSharedPreference.getCONNIOTHUB());*/
        new LogUtils().v( " connString ", connString);
        client = new DeviceClient(connString, protocol);

        //client = new DeviceClient(connString, protocol, publicKeyCertificateString, false, privateKeyString, false);

        try
        {
            client.open();
 /*           if (protocol == IotHubClientProtocol.MQTT)
            {
                MessageCallbackMqtt callback = new MessageCallbackMqtt();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            } else
            {
                MessageCallback callback = new MessageCallback();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            }*/
            //client.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
            //Succeed.set(false);
            //client.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new onProperty(), null);

  /*          do
            {
                Thread.sleep(1000);
            }
            while(!Succeed.get());

            Map<Property, Pair<TwinPropertyCallBack, Object>> desiredProperties = new HashMap<Property, Pair<TwinPropertyCallBack, Object>>()
            {
                {
                    put(new Property("HomeTemp(F)", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                    put(new Property("LivingRoomLights", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                    put(new Property("BedroomRoomLights", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                    put(new Property("HomeSecurityCamera", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                }
            };

            client.subscribeToTwinDesiredProperties(desiredProperties);

            System.out.println("Subscribe to Desired properties on device Twin...");*/
        }
        catch (Exception e2)
        {
            System.err.println("Exception while opening IoTHub connection: " + e2.getMessage());
            client.closeNow();
            System.out.println("Shutting down...");
        }
    }


    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            int switchVal = counter.get() % 3;
            IotHubMessageResult res;
            switch (switchVal)
            {
                case 0:
                    res = IotHubMessageResult.COMPLETE;
                    break;
                case 1:
                    res = IotHubMessageResult.ABANDON;
                    break;
                case 2:
                    res = IotHubMessageResult.REJECT;
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException("Invalid message result specified.");
            }

            System.out.println("Responding to message " + counter.toString() + " with " + res.name());

            counter.increment();

            return res;
        }
    }

    /**
     * Used as a counter in the message callback.
     */
    static class Counter
    {
        int num;

        Counter(int num) {
            this.num = num;
        }

        int get() {
            return this.num;
        }

        void increment() {
            this.num++;
        }

        @Override
        public String toString() {
            return Integer.toString(this.num);
        }
    }


    /**
     * Datos para el envio
     */


    private static Integer msgSentCount  = 0;
    private static Integer receiptsConfirmedCount = 0;
    private static Integer sendFailuresCount  = 0;
    private static Integer noSendCount  = 0;

    class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context)
        {
            Integer i = (Integer) context;
            System.out.println("IoT Hub responded to message " + i.toString()
                    + " with status " + status.name());
            if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY)){
                receiptsConfirmedCount++;
                new FailuresTripValuesRepository(new Application()).deleteValue(i.toString());
            }else{
                sendFailuresCount++;
                System.out.println("NO SE confirmo " + sendFailuresCount
                );
            }
            System.out.println("msgSentCount " + msgSentCount
                    + " receiptsConfirmedCount " + receiptsConfirmedCount
                    + " sendFailuresCount " + sendFailuresCount);
        }
    }

    UtilsLocationService utilsLocationService = new UtilsLocationService();


    Context mcontext;
    public void sendDataJsonString(String idTrip, String msgStr, Context mContext, Integer time){
        //appSharedPreference = new SharedPreference(mContext);

        this.mcontext = mContext;
        String msgStrsss = "[{" +
                "\"tripId\":\""  + deviceId + "\",\"FECHA\":\"" + utilsLocationService.getDateToDay() + "\",\"LONGITUD\":" + "999999969" + ",\"LATITUD\":" + "5858858" + ",\"COUNT\":" + "58" +
                "}," +
                "{\"tripId\":\""  + deviceId + "\",\"FECHA\":\"" + utilsLocationService.getDateToDay() + "\",\"LONGITUD\":" + "999999969" + ",\"LATITUD\":" + "5858858" + ",\"COUNT\":" + "58" +
                "}" +
                "]";
        msgSentCount ++;
//        setRecordTrip(idTrip,"","","","","");


        FailuresTripValuesEntity failuresTripValuesEntity = new FailuresTripValuesEntity();
        failuresTripValuesEntity.setId_trip(idTrip);
        failuresTripValuesEntity.setJson_value(msgStr);
        Integer curretNow = curretToday();
        if (time == 0){
            failuresTripValuesEntity.setTimeCurret(curretNow);
            // GUARDANDO BD
            new LogUtils().v( "System  ADD faile", failuresTripValuesEntity.toString());
            new FailuresTripValuesRepository(new Application()).addFailuresTripValue(failuresTripValuesEntity);
        }else{
            failuresTripValuesEntity.setTimeCurret(Integer.valueOf(time));
        }


        try
        {
            Message msg = new Message(msgStr);
            msg.setMessageId(java.util.UUID.randomUUID().toString());

            EventCallback eventCallback = new EventCallback();
            //client.sendEventAsync(msg, eventCallback, curretNow);
            if (time == 0){
                System.out.println(curretNow + " : " + msgStr);
                client.sendEventAsync(msg, eventCallback,curretNow);
            }else{
                System.out.println(time + " : " + msgStr);
                client.sendEventAsync(msg, eventCallback, Integer.valueOf(time));
            }
        }
        catch (Exception e) {
            noSendCount++;
            System.err.println("Exception while sending event: " + e.getMessage());
                if (new UtilsNetwork().isOnline(mContext)){
                    try {
                        client.open();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }else{
                    System.err.println(" SIN INTERNET ");
                }

        }


    }


    public String getIDTrip(Context context, String deviceId){
        SharedPreference appSharedPreference = new SharedPreference(context);
        HashMap<String, String> idTrip = appSharedPreference.getIdTrip();

        return  deviceId + "-" + idTrip.get(appSharedPreference.getID_TRIP());
    }

    public void resetCount(){
        msgSentCount  = 0;
        receiptsConfirmedCount = 0;
        sendFailuresCount  = 0;
    }

    public void setRecordTrip(String id_trip, String cantidad, String confirmados, String denegados, String no_enviados, String sincronizados) {
        SharedPreference appSharedPreference = new SharedPreference(new Application());
        if (msgSentCount == 1) {
            appSharedPreference = new SharedPreference(new Application());
        }

        appSharedPreference.saveRecordTrip(new FailuresEntity());
        FailuresEntity failuresEntity = appSharedPreference.getRecordTrip();

        if (!id_trip.isEmpty())
            failuresEntity.setId_trip(id_trip);
        if (!cantidad.isEmpty())
            failuresEntity.setCantidad(Integer.parseInt(cantidad));
        if (!confirmados.isEmpty())
            failuresEntity.setCantidad(Integer.parseInt(confirmados));
        if (!denegados.isEmpty())
            failuresEntity.setCantidad(Integer.parseInt(denegados));
        if (!no_enviados.isEmpty())
            failuresEntity.setCantidad(Integer.parseInt(no_enviados));
        if (!sincronizados.isEmpty())
            failuresEntity.setCantidad(Integer.parseInt(sincronizados));

        new LogUtils().v(" failuresEntity ", failuresEntity.toString());

       
    }

    public Integer curretToday(){
        SimpleDateFormat sdf6 = new SimpleDateFormat("H:mm:ss");
        String currentDateandTime = sdf6.format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("MM:dd");
        String currentToDay = sdf.format(new Date());

        new LogUtils().v( "System", currentToDay.trim().replace(":","") + currentDateandTime.trim().replace(":",""));
        return Integer.valueOf(currentToDay.trim().replace(":","")+currentDateandTime.trim().replace(":",""));
    }
}
