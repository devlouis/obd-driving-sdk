package com.mdp.innovation.obd_driving_api.data.entity;



/**
 * Created by louislopez on 13,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */
public class TripEntity  {
    private  String id;
    private  String id_trip;
    private  String live_date;
    private  String latitudd;
    private  String longitud;
    private  String rpm;
    private  String kmh;
    private  String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_trip() {
        return id_trip;
    }

    public void setId_trip(String id_trip) {
        this.id_trip = id_trip;
    }

    public String getLive_date() {
        return live_date;
    }

    public void setLive_date(String live_date) {
        this.live_date = live_date;
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

    @Override
    public String toString() {
        return "TripEntity{" +
                "id='" + id + '\'' +
                ", id_trip='" + id_trip + '\'' +
                ", live_date='" + live_date + '\'' +
                ", latitudd='" + latitudd + '\'' +
                ", longitud='" + longitud + '\'' +
                ", rpm='" + rpm + '\'' +
                ", kmh='" + kmh + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
