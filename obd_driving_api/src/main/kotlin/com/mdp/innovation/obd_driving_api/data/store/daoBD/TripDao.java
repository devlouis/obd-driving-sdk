package com.mdp.innovation.obd_driving_api.data.store.daoBD;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.mdp.innovation.obd_driving_api.data.entity.TripEntity;

import java.util.List;

@Dao
public interface TripDao {
    @Insert
    void insert(TripEntity note);

    @Insert
    void insertAll(TripEntity... notes);

    @Update
    void update(TripEntity note);

    @Update
    public void updateNotes(TripEntity... notes);

    @Delete
    void delete(TripEntity note);

    @Delete
    public void deleteNotes(TripEntity... notes);

    @Query("DELETE FROM tb_trip")
    void deleteAll();

    /**
     Error
     @Query("SELECT * FROM user")
     List<TripEntity> getAll();
     Error:(43, 22) error: There is a problem with the query: [SQLITE_ERROR] SQL error or missing database (no such table: user)
     */

    @Query("SELECT * FROM tb_trip")
    List<TripEntity> getAll();

    @Query("select count(*) from tb_trip")
    long notesCounter();
    /*
    @Query("SELECT * FROM user WHERE age > :minAge")
    @Query("SELECT * FROM user WHERE age BETWEEN :minAge AND :maxAge")
    public User[] loadAllUsersBetweenAges(int minAge, int maxAge);
    @Query("SELECT * FROM user WHERE first_name LIKE :search "
           + "OR last_name LIKE :search")
    public List<User> findUserWithName(String search)
     */

    @Query("SELECT * FROM tb_trip WHERE dataNew = :fecha")
    TripEntity findTripWithDate(String fecha);

    @Query("SELECT *\n" +
            "FROM tb_trip\n" +
            "ORDER BY id ASC\n" +
            "LIMIT :limit, :limit2")
    List<TripEntity> selectFirstTrip(Integer limit, Integer limit2);

    @Query("DELETE FROM tb_trip\n" +
            "WHERE id IN (\n" +
            "    SELECT id FROM (\n" +
            "        SELECT id FROM tb_trip \n" +
            "        ORDER BY id ASC  \n" +
            "        LIMIT 0, :limit\n" +
            "    ) tmp\n" +
            ")")
    void deleteTripWithDate(Integer limit);
}
