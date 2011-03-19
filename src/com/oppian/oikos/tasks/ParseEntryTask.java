package com.oppian.oikos.tasks;

import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

import com.oppian.oikos.OikosManager;
import com.oppian.oikos.OikosParser;
import com.oppian.oikos.model.Entry;

public class ParseEntryTask extends AsyncTask<String, Void, Entry> {

    public ParseEntryTask(ITaskView view, int successResId, int errorResId) {
        super();
        this.view = view;
        this.manager = view.getManager();
        this.successResId = successResId;
        this.errorResId = errorResId;
    }

    private static final String ERROR_MSG = "Database Error";

    private final String LOG_TAG = getClass().getSimpleName();
    
    private ITaskView view;
    
    private int successResId;
    
    private int errorResId;
    
    private OikosManager manager;
    
    @Override
    protected Entry doInBackground(String... params) {
        try {
            return OikosParser.parseEntry(params[0], manager);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, ERROR_MSG);
            throw new RuntimeException(ERROR_MSG, e);
        }
    }
    
    /**
     * The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground()
     */
    protected void onPostExecute(Entry entry) {
        if (entry != null) {
            view.success(successResId);
        } 
        else {
            // display error
            view.error(errorResId);
        }
    }

}
