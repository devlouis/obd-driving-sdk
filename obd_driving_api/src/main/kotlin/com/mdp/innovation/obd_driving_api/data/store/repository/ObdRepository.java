package com.mdp.innovation.obd_driving_api.data.store.repository;

import android.app.Application;
import android.os.AsyncTask;
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity;
import com.mdp.innovation.obd_driving_api.data.store.TripDataBase;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.ObdDao;

import java.util.List;

public class ObdRepository {
    private ObdDao obdDao;
    private List<ObdEntity> obdList;

    public ObdRepository(Application application) {
        TripDataBase db = TripDataBase.getDatabase(application);
        obdDao = db.obdDao();
    }

    public void getAll(PopulateCallback populateCallback) {
        new PopulateAsyncTask(obdDao, populateCallback).execute();
    }

    /**
     * Insertar a BD
     * @param obdEntity
     */
    public void addObd(ObdEntity obdEntity) {
        //noteDao.insert(noteEntity);
        new InsertAsyncTask(obdDao).execute(obdEntity);
    }

    /**
     * Actualizar
     * @param obdEntity
     */
    public void update(ObdEntity obdEntity) {
        new UpdateAsyncTask(obdDao).execute(obdEntity);
    }

    public void deleteAll() {
        //noteDao.delete(noteEntity);
        new DeleteAllAsyncTask(obdDao).execute();
    }

    /**
     * Validar dato existente por segundo
     * @param date
     * @param getWhenDateCallback
     */
    public void getWhereDate(String date, GetWhenDateCallback getWhenDateCallback) {
        //noteDao.delete(noteEntity);
        new getWhenDateAsyncTask(obdDao, getWhenDateCallback).execute(date);
    }

    /**
     * Obtener los primeros datos
     * @param limit
     * @param populateCallback
     */
    public void getFirtsTrips(Integer limit, PopulateCallback populateCallback){
        new GetFirstTripsAsyncTask(obdDao, populateCallback).execute(limit);
    }

    public void deleteFirstObd(Integer limit){
        new DeleteFirstAllAsyncTask(obdDao).execute(limit);
    }


    //Asynctask ------
    public interface PopulateCallback {
        void onSuccess(List<ObdEntity> obdEntityList);
        void onFailure(Exception e);
    }

    public interface GetWhenDateCallback {
        void onSuccess(ObdEntity obdEntity);
        void onFailure();
    }

    private static class PopulateAsyncTask extends AsyncTask<Void, Void, List<ObdEntity>> {

        private final ObdDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        PopulateAsyncTask(ObdDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;
        }

        @Override
        protected List<ObdEntity> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAllObd();
        }

        @Override
        protected void onPostExecute(List<ObdEntity> obdEntityList) {
            super.onPostExecute(obdEntityList);
            if(populateCallback!=null){
                populateCallback.onSuccess(obdEntityList);
            }

        }
    }

    private static class InsertAsyncTask extends AsyncTask<ObdEntity, Void, Void> {

        private final ObdDao mAsyncTaskDao;

        InsertAsyncTask(ObdDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ObdEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<ObdEntity, Void, Void> {

        private final ObdDao mAsyncTaskDao;

        UpdateAsyncTask(ObdDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ObdEntity... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class getWhenDateAsyncTask extends AsyncTask<String, Void, ObdEntity> {

        private final ObdDao mAsyncTaskDao;
        private final GetWhenDateCallback populateCallback;
        getWhenDateAsyncTask(ObdDao dao, GetWhenDateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;

        }
        @Override
        protected ObdEntity doInBackground(String... strings) {
            return mAsyncTaskDao.findObdWithDate(strings[0]);
        }

        @Override
        protected void onPostExecute(ObdEntity obdEntity) {
            if (obdEntity != null){
                super.onPostExecute(obdEntity);
                if(populateCallback!=null){
                    populateCallback.onSuccess(obdEntity);
                }else {
                    populateCallback.onFailure();
                }

            }else{
                populateCallback.onFailure();
            }
        }
    }

    private static class GetFirstTripsAsyncTask extends AsyncTask<Integer, Void, List<ObdEntity>> {

        private final ObdDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        GetFirstTripsAsyncTask(ObdDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;

        }
        @Override
        protected List<ObdEntity> doInBackground(Integer... integers) {
            return mAsyncTaskDao.selectFirstObd(integers[0]);
        }

        @Override
        protected void onPostExecute(List<ObdEntity> obdEntityList) {
            if (obdEntityList != null){
                super.onPostExecute(obdEntityList);
                if(populateCallback!=null){
                    populateCallback.onSuccess(obdEntityList);
                }else {
                    populateCallback.onFailure(new Exception());
                }

            }else{
                populateCallback.onFailure(new Exception());
            }
        }
    }


    private static class DeleteAllAsyncTask extends AsyncTask<ObdEntity, Void, Void> {

        private final ObdDao mAsyncTaskDao;

        DeleteAllAsyncTask(ObdDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ObdEntity... params) {
            mAsyncTaskDao.deleteAllObd();
            return null;
        }
    }

    private static class DeleteFirstAllAsyncTask extends AsyncTask<Integer, Void, Void>{

        private final ObdDao mAsyncTaskDao;

        public DeleteFirstAllAsyncTask(ObdDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            mAsyncTaskDao.deleteObdWithDate(integers[0]);
            return null;
        }
    }


}
