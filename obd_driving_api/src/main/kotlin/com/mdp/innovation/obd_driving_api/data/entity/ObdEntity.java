package com.mdp.innovation.obd_driving_api.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "tb_obd")
public class ObdEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private Long id;
    private String userId = "";
    private String id_trip = "";
    private String vin = "";
    private String rpm = "";
    private String kmh = "";
    private String status = "";
    private String dataNew = "";
    private String dataUdate = "";

    /**
     * Data Sensores*/
    private float ax = 0.0f;
    private float ay = 0.0f;
    private float az = 0.0f;


    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId_trip() {
        return id_trip;
    }

    public void setId_trip(String id_trip) {
        this.id_trip = id_trip;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getRpm() {
        return rpm;
    }

    public void setRpm(String rpm) {
        this.rpm = rpm;
    }

    public String getKmh() {
        return kmh;
    }

    public void setKmh(String kmh) {
        this.kmh = kmh;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataNew() {
        return dataNew;
    }

    public void setDataNew(String dataNew) {
        this.dataNew = dataNew;
    }

    public String getDataUdate() {
        return dataUdate;
    }

    public void setDataUdate(String dataUdate) {
        this.dataUdate = dataUdate;
    }

    public float getAx() {
        return ax;
    }

    public void setAx(float ax) {
        this.ax = ax;
    }

    public float getAy() {
        return ay;
    }

    public void setAy(float ay) {
        this.ay = ay;
    }

    public float getAz() {
        return az;
    }

    public void setAz(float az) {
        this.az = az;
    }

    @Override
    public String toString() {
        return "ObdEntity{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", id_trip='" + id_trip + '\'' +
                ", vin='" + vin + '\'' +
                ", rpm='" + rpm + '\'' +
                ", kmh='" + kmh + '\'' +
                ", status='" + status + '\'' +
                ", dataNew='" + dataNew + '\'' +
                ", dataUdate='" + dataUdate + '\'' +
                ", ax=" + ax +
                ", ay=" + ay +
                ", az=" + az +
                '}';
    }
}
