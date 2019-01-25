package com.mdp.innovation.obd_driving_api.data.store.daoBD;

import android.arch.persistence.room.*;
import com.mdp.innovation.obd_driving_api.data.entity.FailuresTripValuesEntity;


import java.util.List;

@Dao
public interface FailuresTripValuesDao {

    @Insert
    void insert(FailuresTripValuesEntity failuresTripValuesEntity);

    @Insert
    void insertAll(FailuresTripValuesEntity... failuresTripValuesEntity);

    @Update
    void update(FailuresTripValuesEntity failuresTripValuesEntity);

    @Delete
    void delete(FailuresTripValuesEntity failuresTripValuesEntity);

    @Query("DELETE FROM tb_failures_trip_values")
    void deleteAll();

    @Query("SELECT * FROM tb_failures_trip_values")
    List<FailuresTripValuesEntity> getAll();

    @Query("DELETE FROM tb_failures_trip_values WHERE id_trip_values = :value")
    void deleteValue(String value);
}
