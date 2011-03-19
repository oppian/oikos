package com.oppian.oikos.tasks;

import java.sql.SQLException;

import android.os.AsyncTask;

import com.oppian.oikos.OikosManager;
import com.oppian.oikos.OikosParser;
import com.oppian.oikos.model.Entry;

public class AddCashTask extends AsyncTask<String, Void, Entry> {

    private OikosManager manager;

    private ITaskView view;
    
    public AddCashTask(ITaskView view, OikosManager manager) {
        super();
        this.view = view;
        this.manager = manager;
    }

    @Override
    protected Entry doInBackground(String... params) {
        Number amount = OikosParser.parseAmount(OikosParser.numberFormats(), params[0]);
        String description = params[1];
        if (amount != null) {
            try {
                return manager.addEntry(OikosParser.numberToCurrencyInt(amount) * -1, description);
            } catch (SQLException e) {
                // TODO output error
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Entry result) {
        if (result != null) {
            view.refresh();
        }
        // TODO output no result
    }
    
    

}
