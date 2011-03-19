package com.oppian.oikos.tasks;

import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

import com.oppian.oikos.OikosManager;
import com.oppian.oikos.OikosParser;
import com.oppian.oikos.model.Entry;

public class AddCashTask extends AsyncTask<String, Void, Entry> {

    private static final String ERROR_MSG = "Database Error";

    private final String LOG_TAG = getClass().getSimpleName();
    
    public AddCashTask(ITaskView view, int successResId, int errorResId) {
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
    protected Entry doInBackground(String... params) {
        Number amount = OikosParser.parseAmount(OikosParser.numberFormats(), params[0]);
        String description = params[1];
        if (amount != null) {
            try {
                return manager.addEntry(OikosParser.numberToCurrencyInt(amount) * -1, description);
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, ERROR_MSG);
                throw new RuntimeException(ERROR_MSG, e);
            }
        }
        return null;
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
