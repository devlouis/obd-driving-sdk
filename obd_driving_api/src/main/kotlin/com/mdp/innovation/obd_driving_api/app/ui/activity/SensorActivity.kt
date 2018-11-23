package com.mdp.innovation.obd_driving_api.app.ui.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import kotlinx.android.synthetic.main.activity_sensor.*

class SensorActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName

    lateinit var mSensorManager : SensorManager
    lateinit var mSensorAcc : Sensor
    lateinit var mSensorGyr : Sensor
    lateinit var mSensorMgt : Sensor
    lateinit var sensorEventListener: SensorEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)
        initSensor()
    }

    fun initSensor(){
        /**
         * obtener sensores para usar
         */
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mSensorMgt = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        /**
         * validar si existe sensor
         */
        if (mSensorAcc == null)
            LogUtils().v(TAG, " No se encuentra sensor de Acelerometro" )
        if (mSensorGyr == null)
            LogUtils().v(TAG, " No se encuentra sensor de Giroscopio" )
        if (mSensorMgt == null)
            LogUtils().v(TAG, " No se encuentra sensor de Magnetometro" )
        initUI()
    }

    fun initUI(){
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when(event.sensor.type){
                    Sensor.TYPE_ACCELEROMETER -> {
                        tviAceX.text =  "X = ${"%.6f".format(event.values[0])}"
                        tviAceY.text =  "Y = ${"%.6f".format(event.values[1])}"
                        tviAceZ.text =  "Z = ${"%.6f".format(event.values[1])}"
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        tviGiroX.text =  "X = ${"%.6f".format(event.values[0])}"
                        tviGiroY.text =  "Y = ${"%.6f".format(event.values[1])}"
                        tviGiroZ.text =  "Z = ${"%.6f".format(event.values[2])}"
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        tviMagneX.text =  "X = ${"%.6f".format(event.values[0])}"
                        tviMagneY.text =  "Y = ${"%.6f".format(event.values[1])}"
                        tviMagneZ.text =  "Z = ${"%.6f".format(event.values[2])}"
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, int: Int) {

            }
        }
        startSensor()
    }

    fun startSensor(){
        mSensorManager.registerListener(sensorEventListener, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(sensorEventListener, mSensorGyr, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(sensorEventListener, mSensorMgt, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopSensor(){
        mSensorManager.unregisterListener(sensorEventListener)
    }
}
