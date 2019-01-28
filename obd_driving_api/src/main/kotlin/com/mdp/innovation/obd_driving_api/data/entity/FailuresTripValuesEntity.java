package com.mdp.innovation.obd_driving_api.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "tb_failures_trip_values")
public class FailuresTripValuesEntity {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private Long id;
    private String timeCurret = "";
    private String id_trip = "";
    private String id_trip_values = "";
    private String json_value = "";


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

    public String getId_trip_values() {
        return id_trip_values;
    }

    public void setId_trip_values(String id_trip_values) {
        this.id_trip_values = id_trip_values;
    }

    public String getJson_value() {
        return json_value;
    }

    public void setJson_value(String json_value) {
        this.json_value = json_value;
    }

    public String getTimeCurret() {
        return timeCurret;
    }

    public void setTimeCurret(String timeCurret) {
        this.timeCurret = timeCurret;
    }

    @Override
    public String toString() {
        return "FailuresTripValuesEntity{" +
                "id=" + id +
                ", timeCurret='" + timeCurret + '\'' +
                ", id_trip='" + id_trip + '\'' +
                ", id_trip_values='" + id_trip_values + '\'' +
                ", json_value='" + json_value + '\'' +
                '}';
    }
}
