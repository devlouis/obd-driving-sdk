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
    private String live_date = "";
    private String lat = "";
    private String lon = "";
    private String bearing = "";
    private String rpm = "";
    private String speed = "";
    private String status = "";
    private String time = "";
    private String dataUdate = "";
    private long dataNewMili = 0;
    private long dataUdateMili = 0;


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

    public String getLive_date() {
        return live_date;
    }

    public void setLive_date(String live_date) {
        this.live_date = live_date;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getRpm() {
        return rpm;
    }

    public void setRpm(String rpm) {
        this.rpm = rpm;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
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

    public long getDataNewMili() {
        return dataNewMili;
    }

    public void setDataNewMili(long dataNewMili) {
        this.dataNewMili = dataNewMili;
    }

    public long getDataUdateMili() {
        return dataUdateMili;
    }

    public void setDataUdateMili(long dataUdateMili) {
        this.dataUdateMili = dataUdateMili;
    }

    @Override
    public String toString() {
        return "TripEntity{" +
                "id=" + id +
                ", tripId='" + tripId + '\'' +
                ", live_date='" + live_date + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", bearing='" + bearing + '\'' +
                ", rpm='" + rpm + '\'' +
                ", speed='" + speed + '\'' +
                ", status='" + status + '\'' +
                ", time='" + time + '\'' +
                ", dataUdate='" + dataUdate + '\'' +
                ", dataNewMili=" + dataNewMili +
                ", dataUdateMili=" + dataUdateMili +
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
