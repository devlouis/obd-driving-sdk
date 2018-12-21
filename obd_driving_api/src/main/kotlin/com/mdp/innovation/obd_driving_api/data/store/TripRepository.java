package com.mdp.innovation.obd_driving_api.data.store;

import android.app.Application;
import android.os.AsyncTask;
import com.mdp.innovation.obd_driving_api.data.entity.TripEntity;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.TripDao;

import java.util.List;

public class TripRepository {

    private TripDao tripDao;
    private List<TripEntity> notes;

    public TripRepository(Application application) {
        TripDataBase db = TripDataBase.getDatabase(application);
        tripDao = db.tripDao();
        // notes = noteDao.getAll();
    }

    public void getAllNotes(PopulateCallback populateCallback) {
        //return notes = noteDao.getAll();
        /*new PopulateAsyncTask(noteDao, new PopulateCallback() {
            @Override
            public void onSuccess(List<NoteEntity> noteEntities) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        }).execute();*/
        new PopulateAsyncTask(tripDao, populateCallback).execute();
    }

    public void add(TripEntity tripEntity) {
        //noteDao.insert(noteEntity);
        new InsertAsyncTask(tripDao).execute(tripEntity);
    }

    /**
     * Error
     * Caused by: android.database.sqlite.SQLiteConstraintException: UNIQUE constraint failed: tb_notes.id (code 1555)
     */
    public void addNotes(TripEntity... tripEntityList) {
        //noteDao.insertAll(noteList);
        new InsertEntitiesAsyncTask(tripDao).execute(tripEntityList);
    }

    public void update(TripEntity tripEntity) {
        //noteDao.update(noteEntity);
        new UpdateAsyncTask(tripDao).execute(tripEntity);
    }

    public void delete(TripEntity tripEntity) {
        //noteDao.delete(noteEntity);
        new DeleteAsyncTask(tripDao).execute(tripEntity);
    }

    public void deleteAll() {
        //noteDao.delete(noteEntity);
        new DeleteAllAsyncTask(tripDao).execute();
    }

    public void getWhereDate(String date, GetWhenDateCallback getWhenDateCallback) {
        //noteDao.delete(noteEntity);
        new getWhenDateAsyncTask(tripDao, getWhenDateCallback).execute(date);
    }

    public void getFirtsTrips(Integer limit, PopulateCallback populateCallback){
        new GetFirstTripsAsyncTask(tripDao, populateCallback).execute(limit);
    }


    //Asynctask ------

    public interface PopulateCallback {
        void onSuccess(List<TripEntity> tripEntityList);
        void onFailure(Exception e);
    }

    public interface GetWhenDateCallback {
        void onSuccess(TripEntity tripEntity);
        void onFailure();
    }

    private static class InsertAsyncTask extends AsyncTask<TripEntity, Void, Void> {

        private final TripDao mAsyncTaskDao;

        InsertAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TripEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<TripEntity, Void, Void> {

        private final TripDao mAsyncTaskDao;

        UpdateAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TripEntity... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<TripEntity, Void, Void> {

        private final TripDao mAsyncTaskDao;

        DeleteAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TripEntity... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
    private static class DeleteAllAsyncTask extends AsyncTask<TripEntity, Void, Void> {

        private final TripDao mAsyncTaskDao;

        DeleteAllAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TripEntity... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    private static class InsertEntitiesAsyncTask extends AsyncTask<TripEntity, Void, Void> {

        private final TripDao mAsyncTaskDao;

        InsertEntitiesAsyncTask(TripDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TripEntity... params) {
            mAsyncTaskDao.insertAll(params);
            return null;
        }
    }

    private static class PopulateAsyncTask extends AsyncTask<Void, Void, List<TripEntity>> {

        private final TripDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        PopulateAsyncTask(TripDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;
        }

        @Override
        protected List<TripEntity> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAll();
        }

        @Override
        protected void onPostExecute(List<TripEntity> noteEntities) {
            super.onPostExecute(noteEntities);
            if(populateCallback!=null){
                populateCallback.onSuccess(noteEntities);
            }

        }
    }

    private static class getWhenDateAsyncTask extends AsyncTask<String, Void, TripEntity> {

        private final TripDao mAsyncTaskDao;
        private final GetWhenDateCallback populateCallback;
        getWhenDateAsyncTask(TripDao dao, GetWhenDateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;

        }
        @Override
        protected TripEntity doInBackground(String... strings) {
            return mAsyncTaskDao.findTripWithDate(strings[0]);
        }

        @Override
        protected void onPostExecute(TripEntity tripEntity) {
            if (tripEntity != null){
                super.onPostExecute(tripEntity);
                if(populateCallback!=null){
                    populateCallback.onSuccess(tripEntity);
                }else {
                    populateCallback.onFailure();
                }

            }else{
                populateCallback.onFailure();
            }
        }
    }

    private static class GetFirstTripsAsyncTask extends AsyncTask<Integer, Void, List<TripEntity>> {

        private final TripDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        GetFirstTripsAsyncTask(TripDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;

        }
        @Override
        protected List<TripEntity> doInBackground(Integer... integers) {
            return mAsyncTaskDao.selectFirstTrip(integers[0]);
        }

        @Override
        protected void onPostExecute(List<TripEntity> tripEntityList) {
            if (tripEntityList != null){
                super.onPostExecute(tripEntityList);
                if(populateCallback!=null){
                    populateCallback.onSuccess(tripEntityList);
                }else {
                    populateCallback.onFailure(new Exception());
                }

            }else{
                populateCallback.onFailure(new Exception());
            }
        }
    }




}
