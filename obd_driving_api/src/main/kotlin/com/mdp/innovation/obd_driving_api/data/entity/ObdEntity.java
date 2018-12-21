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
    private String id_trip = "";
    private String rpm = "";
    private String kmh = "";
    private String status = "";
    private String dataNew = "";
    private String dataUdate = "";

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getId_trip() {
        return id_trip;
    }

    public void setId_trip(String id_trip) {
        this.id_trip = id_trip;
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

    @Override
    public String toString() {
        return "ObdEntity{" +
                "id=" + id +
                ", id_trip='" + id_trip + '\'' +
                ", rpm='" + rpm + '\'' +
                ", kmh='" + kmh + '\'' +
                ", status='" + status + '\'' +
                ", dataNew='" + dataNew + '\'' +
                ", dataUdate='" + dataUdate + '\'' +
                '}';
    }
}
