package com.oppian.oikos.tasks;

import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

import com.oppian.oikos.OikosManager;
import com.oppian.oikos.model.Entry;

public class DeleteEntryTask extends AsyncTask<Integer, Void, Entry> {

    private static final String ERROR_MSG = "Database Error";

    private final String LOG_TAG = getClass().getSimpleName();
    
    public DeleteEntryTask(ITaskView view, int successResId, int errorResId) {
        super();
        this.view = view;
        this.manager = view.getManager();
        this.successResId = successResId;
        this.errorResId = errorResId;
    }

    private ITaskView view;
    
    private int successResId;
    
    private int errorResId;
    
    private OikosManager manager;

    @Override
    protected Entry doInBackground(Integer... params) {
        Integer position = params[0];
            try {
                return manager.removeEntry(position);
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, ERROR_MSG);
                throw new RuntimeException(ERROR_MSG, e);
            }
    }

    @Override
    protected void onPostExecute(Entry entry) {
        if (entry != null) {
            view.success(successResId);
        } else {
            view.error(errorResId);
        }
    }
    
    

}
