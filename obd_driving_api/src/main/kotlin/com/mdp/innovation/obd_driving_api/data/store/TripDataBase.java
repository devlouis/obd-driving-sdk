package com.mdp.innovation.obd_driving_api.data.store;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import com.mdp.innovation.obd_driving_api.data.entity.LocationEntity;
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity;
import com.mdp.innovation.obd_driving_api.data.entity.TripEntity;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.LocationDao;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.ObdDao;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.TripDao;


@Database(entities = {TripEntity.class, ObdEntity.class, LocationEntity.class}, version = 15)
public  abstract class TripDataBase extends RoomDatabase {
    public abstract TripDao tripDao();
    public abstract ObdDao obdDao();
    public abstract LocationDao locationDao();
    private static TripDataBase INSTANCE;

    public static TripDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TripDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TripDataBase.class, "BDRoomTrip")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this codelab.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     */
    private static Callback sRoomDatabaseCallback = new Callback(){

        @Override
        public void onOpen (@NonNull SupportSQLiteDatabase db){
            super.onOpen(db);
            // If you want to keep the data through app restarts,
            // comment out the following line.
            //clearNotes(INSTANCE);
        }

    };

    private static void clearTrip(@NonNull TripDataBase db) {
        TripDao noteDao= db.tripDao();
        noteDao.deleteAll();
    }
}
