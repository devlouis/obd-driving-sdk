package com.mdp.innovation.obd_driving_api.data.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by louislopez on 13,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */

@Entity(tableName = "tb_trip")
public class TripEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private Long id;

    private String tripId = "";
    private String vin = "";
    private Float lat = 0.0f;
    private Float lon = 0.0f;
    private Float bearing = 0.0f;
    private Integer rpm = 0;
    private Integer speed = -1;
    private String status = "";
    private String time = "";
    private String dataUdate = "";
    /**
     * Data Sensores*/
    private float ax = 0.0f;
    private float ay = 0.0f;
    private float az = 0.0f;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public Float getBearing() {
        return bearing;
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }

    public Integer getRpm() {
        return rpm;
    }

    public void setRpm(Integer rpm) {
        this.rpm = rpm;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
        return "TripEntity{" +
                "id=" + id +
                ", tripId='" + tripId + '\'' +
                ", vin='" + vin + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", bearing=" + bearing +
                ", rpm=" + rpm +
                ", speed=" + speed +
                ", status='" + status + '\'' +
                ", time='" + time + '\'' +
                ", dataUdate='" + dataUdate + '\'' +
                ", ax=" + ax +
                ", ay=" + ay +
                ", az=" + az +
                '}';
    }

/*@Override
    public String toString() {
        return "{" +
                "'id':'" + id + "',"+
                "'tripId':'" + tripId + "'," +
                "'live_date':'" + live_date + "'," +
                "'lat':'" + lat + "'," +
                "'lon':'" + lon + "'," +
                "'rpm':'" + rpm + "'," +
                "'speed':'" + speed + "'," +
                "'status':'" + status + "'," +
                "'time':'" + time + "'," +
                "'dataUdate':'" + dataUdate + "'," +
                '}';
    }*/
}
