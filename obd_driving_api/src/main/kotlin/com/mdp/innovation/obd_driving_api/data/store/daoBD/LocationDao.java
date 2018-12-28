package com.mdp.innovation.obd_driving_api.data.store.daoBD;

import android.arch.persistence.room.*;
import com.mdp.innovation.obd_driving_api.data.entity.LocationEntity;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    void insert(LocationEntity locationEntity);

    @Insert
    void insertAll(LocationEntity... locationEntity);

    @Update
    void update(LocationEntity locationEntity);

    @Update
    public void updateNotes(LocationEntity... locationEntity);

    @Delete
    void delete(LocationEntity locationEntity);

    @Delete
    public void deleteNotes(LocationEntity... locationEntity);

    @Query("DELETE FROM tb_location")
    void deleteAll();

    @Query("SELECT * FROM tb_location")
    List<LocationEntity> getAll();

    @Query("SELECT * FROM tb_location WHERE dataNew = :fecha")
    LocationEntity findWithDate(String fecha);

    @Query("SELECT *\n" +
            "FROM tb_location\n" +
            "ORDER BY id ASC\n" +
            "LIMIT :limit")
    List<LocationEntity> selectFirstLocation(Integer limit);


    @Query("DELETE FROM tb_location\n" +
            "WHERE id IN (\n" +
            "    SELECT id FROM (\n" +
            "        SELECT id FROM tb_location \n" +
            "        ORDER BY id ASC  \n" +
            "        LIMIT 0, :limit\n" +
            "    ) tmp\n" +
            ")")
    void deleteLocationWithDate(Integer limit);


/*    @Query("SELECT COUNT(dataNew)\n" +
            "  FROM tb_trip")
    Integer countLocation();*/
}
