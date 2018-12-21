package com.mdp.innovation.obd_driving_api.data.store.daoBD;

import android.arch.persistence.room.*;
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity;

import java.util.List;

@Dao
public interface ObdDao {

    @Insert
    void insert(ObdEntity obdEntity);

    @Insert
    void insertAll(ObdEntity... obdEntity);

    @Update
    void update(ObdEntity obdEntity);

    @Update
    public void updateObd(ObdEntity... obdEntity);

    @Delete
    void delete(ObdEntity obdEntity);

    @Delete
    public void deleteObd(ObdEntity... obdEntity);

    @Query("DELETE FROM tb_obd")
    void deleteAllObd();

    @Query("SELECT * FROM tb_obd")
    List<ObdEntity> getAllObd();

    @Query("SELECT * FROM tb_obd WHERE dataNew = :fecha")
    ObdEntity findObdWithDate(String fecha);

    @Query("SELECT *\n" +
            "FROM tb_obd\n" +
            "ORDER BY id ASC\n" +
            "LIMIT :limit")
    List<ObdEntity> selectFirstObd(Integer limit);


    @Query("DELETE FROM tb_obd\n" +
            "WHERE id IN (\n" +
            "    SELECT id FROM (\n" +
            "        SELECT id FROM tb_obd \n" +
            "        ORDER BY id ASC  \n" +
            "        LIMIT 0, :limit\n" +
            "    ) tmp\n" +
            ")")
    void deleteObdWithDate(Integer limit);
}
