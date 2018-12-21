package com.mdp.innovation.obd_driving_api.data.store;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.ObdDao;

@Database(entities = {ObdEntity.class}, version = 1)
public abstract class ObdDataBase extends RoomDatabase {
    public abstract ObdDao obdDao();
    private static ObdDataBase INSTANCE;

    public static ObdDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ObdDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ObdDataBase.class, "BDRoomTrip")
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

}