package com.oppian.oikos;

import java.sql.SQLException;
import java.text.NumberFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager.SqliteOpenHelperFactory;
import com.oppian.oikos.adaptors.EntryAdapter;
import com.oppian.oikos.tasks.AddCashTask;
import com.oppian.oikos.tasks.ITaskView;

public class OikosMainActivity extends OrmLiteBaseListActivity<Db> implements ITaskView {

    static {
        OpenHelperManager.setOpenHelperFactory(new SqliteOpenHelperFactory() {
            public OrmLiteSqliteOpenHelper getHelper(Context context) {
                return new Db(context);
            }
        });
    }

    private TextView     addEntryTextView;

    private final String LOG_TAG = getClass().getSimpleName();

    private OikosManager manager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // call parent onCreate
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "creating " + getClass());
        setContentView(R.layout.main);

        createManager();

        // add us as observer
        setupViews();
        refreshViews();
    }



    private boolean parseEntry(String text) {
        try {
            return OikosParser.parseEntry(text, manager);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, getString(R.string.error_sql), e);
            throw new RuntimeException(getString(R.string.error_sql), e);
        }
    }

    private void refreshViews() {
        if (manager == null) { return; }
        // get number format
        NumberFormat nf = NumberFormat.getCurrencyInstance();

        // display total
        TextView totalTextView = (TextView) findViewById(R.id.total);
        totalTextView.setText(nf.format(manager.getAccount().getTotal() / 100.0));

        // display average
        TextView averageTextView = (TextView) findViewById(R.id.perday);
        averageTextView.setText(nf.format(manager.getAverage() / 100.0) + "/day");

        // display list be setting the adaptor
        setListAdapter(new EntryAdapter(this, R.layout.list_item, manager.getEntryList(), nf));
    }

    private void setupViews() {
        // setup click handlers for list
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        // setup add textview
        addEntryTextView = (TextView) findViewById(R.id.addEntry);
        addEntryTextView.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // disable tv
                addEntryTextView.setEnabled(false);
                // parse in bg
                new ParseTask().execute(addEntryTextView.getText().toString());
                return true; // eat the key
            }
        });
    }

    protected void createManager() {
        if (manager != null) { return; }
        // create manager
        try {
            manager = new OikosManager(getHelper());
        } catch (SQLException e) {
            Log.e(LOG_TAG, getString(R.string.error_sql), e);
            e.printStackTrace();
            // change to runtime exception
            throw new RuntimeException(getString(R.string.error_sql), e);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle selection
        switch (item.getItemId()) {
            case R.id.addCash:
                addCash();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void addCash() {
        // show add cash dialog
        Log.i(LOG_TAG, "addCash");
        // view call back for task
        final ITaskView view = this;

        // build alert dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.add_cash)
            .setMessage(R.string.amount);
        
        // edit text to get input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        alert.setView(input);
        
        // ok button
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // create task
                AddCashTask task = new AddCashTask(view, manager);
                // execute
                task.execute(input.getText().toString(), 
                        getString(R.string.add_cash_entry_description));
            }
        });
        
        // cancel
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // cancelled
            }
        });
        
        alert.show();
    }

    private class ParseTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... entry) {
            return parseEntry(entry[0]);
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Boolean clear) {
            if (clear) {
                addEntryTextView.setText("");
                refreshViews();
            }
            addEntryTextView.setEnabled(true);
        }
    }

    public void refresh() {
        refreshViews();
    }
}
