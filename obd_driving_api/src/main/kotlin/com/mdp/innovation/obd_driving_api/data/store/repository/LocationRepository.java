package com.mdp.innovation.obd_driving_api.data.store.repository;

import android.app.Application;
import android.os.AsyncTask;
import com.mdp.innovation.obd_driving_api.data.entity.LocationEntity;
import com.mdp.innovation.obd_driving_api.data.store.LocationDataBase;
import com.mdp.innovation.obd_driving_api.data.store.TripDataBase;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.LocationDao;

import java.util.List;

public class LocationRepository {
    LocationDao locationDao;
    List<LocationEntity> locationEntityList;

    public LocationRepository(Application application) {
        TripDataBase db = TripDataBase.getDatabase(application);
        locationDao = db.locationDao();
    }

    public void getAll(PopulateCallback populateCallback) {
        new PopulateAsyncTask(locationDao, populateCallback).execute();
    }

    /**
     * Insertar a BD
     * @param locationEntity
     */
    public void addLocation(LocationEntity locationEntity) {
        //noteDao.insert(noteEntity);
        new InsertAsyncTask(locationDao).execute(locationEntity);
    }

    /**
     * Actualizar
     * @param locationEntity
     */
    public void update(LocationEntity locationEntity) {
        new UpdateAsyncTask(locationDao).execute(locationEntity);
    }

    public void deleteAll() {
        //noteDao.delete(noteEntity);
        new DeleteAllAsyncTask(locationDao).execute();
    }

    /**
     * Validar dato existente por segundo
     * @param date
     * @param getWhenDateCallback
     */
    public void getWhereDate(String date, GetWhenDateCallback getWhenDateCallback) {
        //noteDao.delete(noteEntity);
        new getWhenDateAsyncTask(locationDao, getWhenDateCallback).execute(date);
    }

    /**
     * Obtener los primeros datos
     * @param limit
     * @param populateCallback
     */
    public void getFirtsTrips(Integer limit, PopulateCallback populateCallback){
        new GetFirstTripsAsyncTask(locationDao, populateCallback).execute(limit);
    }

    public void deleteFirstLocation(Integer limit){
        new DeleteFirstAllAsyncTask(locationDao).execute(limit);
    }

    //Asynctask ------
    public interface PopulateCallback {
        void onSuccess(List<LocationEntity> locationEntityList);
        void onFailure(Exception e);
    }

    public interface GetWhenDateCallback {
        void onSuccess(LocationEntity obdEntity);
        void onFailure();
    }

    private static class PopulateAsyncTask extends AsyncTask<Void, Void, List<LocationEntity>> {

        private final LocationDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        PopulateAsyncTask(LocationDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;
        }

        @Override
        protected List<LocationEntity> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAll();
        }

        @Override
        protected void onPostExecute(List<LocationEntity> obdEntityList) {
            super.onPostExecute(obdEntityList);
            if(populateCallback!=null){
                populateCallback.onSuccess(obdEntityList);
            }

        }
    }

    private static class InsertAsyncTask extends AsyncTask<LocationEntity, Void, Void> {

        private final LocationDao mAsyncTaskDao;

        InsertAsyncTask(LocationDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final LocationEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<LocationEntity, Void, Void> {

        private final LocationDao mAsyncTaskDao;

        UpdateAsyncTask(LocationDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final LocationEntity... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class getWhenDateAsyncTask extends AsyncTask<String, Void, LocationEntity> {

        private final LocationDao mAsyncTaskDao;
        private final GetWhenDateCallback populateCallback;
        getWhenDateAsyncTask(LocationDao dao, GetWhenDateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;

        }
        @Override
        protected LocationEntity doInBackground(String... strings) {
            return mAsyncTaskDao.findWithDate(strings[0]);
        }

        @Override
        protected void onPostExecute(LocationEntity locationEntity) {
            if (locationEntity != null){
                super.onPostExecute(locationEntity);
                if(populateCallback!=null){
                    populateCallback.onSuccess(locationEntity);
                }else {
                    populateCallback.onFailure();
                }

            }else{
                populateCallback.onFailure();
            }
        }
    }

    private static class GetFirstTripsAsyncTask extends AsyncTask<Integer, Void, List<LocationEntity>> {

        private final LocationDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        GetFirstTripsAsyncTask(LocationDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;

        }
        @Override
        protected List<LocationEntity> doInBackground(Integer... integers) {
            return mAsyncTaskDao.selectFirstLocation(integers[0]);
        }

        @Override
        protected void onPostExecute(List<LocationEntity> locationEntityList) {
            if (locationEntityList != null){
                super.onPostExecute(locationEntityList);
                if(populateCallback!=null){
                    populateCallback.onSuccess(locationEntityList);
                }else {
                    populateCallback.onFailure(new Exception());
                }

            }else{
                populateCallback.onFailure(new Exception());
            }
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<LocationEntity, Void, Void> {

        private final LocationDao mAsyncTaskDao;

        DeleteAllAsyncTask(LocationDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final LocationEntity... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    private class DeleteFirstAllAsyncTask extends AsyncTask<Integer, Void, Void>{

        private final LocationDao mAsyncTaskDao;

        public DeleteFirstAllAsyncTask(LocationDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            mAsyncTaskDao.deleteLocationWithDate(integers[0]);
            return null;
        }
    }
}
