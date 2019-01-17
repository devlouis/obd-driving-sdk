package com.mdp.innovation.obd_driving_api.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "tb_location")
public class LocationEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private Long id;

    private String id_trip = "";
    private String latitudd = "";
    private String longitud = "";
    private String bearing = "";
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

    public String getLatitudd() {
        return latitudd;
    }

    public void setLatitudd(String latitudd) {
        this.latitudd = latitudd;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
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
        return "LocationEntity{" +
                "id=" + id +
                ", id_trip='" + id_trip + '\'' +
                ", latitudd='" + latitudd + '\'' +
                ", longitud='" + longitud + '\'' +
                ", bearing='" + bearing + '\'' +
                ", status='" + status + '\'' +
                ", dataNew='" + dataNew + '\'' +
                ", dataUdate='" + dataUdate + '\'' +
                '}';
    }
}
