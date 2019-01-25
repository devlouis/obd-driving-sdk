package com.mdp.innovation.obd_driving_api.data.store.repository;

import android.app.Application;
import android.os.AsyncTask;
import com.mdp.innovation.obd_driving_api.data.entity.FailuresTripValuesEntity;
import com.mdp.innovation.obd_driving_api.data.store.TripDataBase;
import com.mdp.innovation.obd_driving_api.data.store.daoBD.FailuresTripValuesDao;

import java.util.List;

public class FailuresTripValuesRepository {
    FailuresTripValuesDao failuresTripValuesDao;

    public FailuresTripValuesRepository(Application application) {
        TripDataBase db = TripDataBase.getDatabase(application);
        this.failuresTripValuesDao = db.failuresTripValuesDao();
    }

    public void getAll(PopulateCallback populateCallback) {
        new PopulateAsyncTask(failuresTripValuesDao, populateCallback).execute();
    }

    /**
     * Insertar a BD
     * @param failuresTripValuesEntity
     */
    public void addFailuresTripValue(FailuresTripValuesEntity failuresTripValuesEntity) {
        //noteDao.insert(noteEntity);
        new InsertAsyncTask(failuresTripValuesDao).execute(failuresTripValuesEntity);
    }

    /**
     * Actualizar
     * @param failuresTripValuesEntity
     */
    public void update(FailuresTripValuesEntity failuresTripValuesEntity) {
        new UpdateAsyncTask(failuresTripValuesDao).execute(failuresTripValuesEntity);
    }

    /**
     * Eliminar
     * @param failuresTripValuesEntity
     */
    public void deleteFailuresTripValue(FailuresTripValuesEntity failuresTripValuesEntity) {
        new DeleteAsyncTask(failuresTripValuesDao).execute(failuresTripValuesEntity);
    }

    public void deleteAll() {
        //noteDao.delete(noteEntity);
        new DeleteAllAsyncTask(failuresTripValuesDao).execute();
    }
    public void deleteValue(String value) {
        //noteDao.delete(noteEntity);
        new DeleteValueAsyncTask(failuresTripValuesDao).execute(value);
    }



    //Asynctask ------
    public interface PopulateCallback {
        void onSuccess(List<FailuresTripValuesEntity> failuresTripValuesEntityList);
        void onFailure(Exception e);
    }


    private static class PopulateAsyncTask extends AsyncTask<Void, Void, List<FailuresTripValuesEntity>> {

        private final FailuresTripValuesDao mAsyncTaskDao;
        private final PopulateCallback populateCallback;

        PopulateAsyncTask(FailuresTripValuesDao dao, PopulateCallback populateCallback) {
            mAsyncTaskDao = dao;
            this.populateCallback = populateCallback;
        }

        @Override
        protected List<FailuresTripValuesEntity> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAll();
        }

        @Override
        protected void onPostExecute(List<FailuresTripValuesEntity> obdEntityList) {
            super.onPostExecute(obdEntityList);
            if(populateCallback!=null){
                populateCallback.onSuccess(obdEntityList);
            }

        }
    }

    private static class InsertAsyncTask extends AsyncTask<FailuresTripValuesEntity, Void, Void> {

        private final FailuresTripValuesDao mAsyncTaskDao;

        InsertAsyncTask(FailuresTripValuesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FailuresTripValuesEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<FailuresTripValuesEntity, Void, Void> {

        private final FailuresTripValuesDao mAsyncTaskDao;

        UpdateAsyncTask(FailuresTripValuesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FailuresTripValuesEntity... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }
    private static class DeleteAsyncTask extends AsyncTask<FailuresTripValuesEntity, Void, Void> {

        private final FailuresTripValuesDao mAsyncTaskDao;

        DeleteAsyncTask(FailuresTripValuesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FailuresTripValuesEntity... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<FailuresTripValuesEntity, Void, Void> {

        private final FailuresTripValuesDao mAsyncTaskDao;

        DeleteAllAsyncTask(FailuresTripValuesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FailuresTripValuesEntity... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
    private static class DeleteValueAsyncTask extends AsyncTask<String, Void, Void> {

        private final FailuresTripValuesDao mAsyncTaskDao;

        DeleteValueAsyncTask(FailuresTripValuesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteValue(params[0]);
            return null;
        }
    }


}
