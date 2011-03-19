package com.oppian.oikos;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager.SqliteOpenHelperFactory;
import com.oppian.oikos.adaptors.EntryAdapter;

public class Oikos extends OrmLiteBaseListActivity<Db> {

    private class OikosParseTask extends AsyncTask<String, Void, Boolean> {

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

    private Number parseAmount(NumberFormat[] numberFormats, String token) {
        // try parse using currency
        for (NumberFormat numberFormat : numberFormats) {
            try {
                return numberFormat.parse(token);
            } catch (ParseException e) {
                // ignore exception
            }
        }
        return null;
    }

    private boolean parseEntry(String text) {
        // get number format
        NumberFormat cf = NumberFormat.getCurrencyInstance();
        NumberFormat nf = NumberFormat.getNumberInstance();
        NumberFormat nfDefault = NumberFormat.getNumberInstance(Locale.US);
        NumberFormat[] numberFormats = new NumberFormat[] { cf, nf, nfDefault };
        StringBuilder description = new StringBuilder();
        Number amount = null;
        // tokenize string on whitespace
        String[] tokens = text.split("\\s");
        String token = null;
        for (int x = 0; x < tokens.length; x++) {
            token = tokens[x];
            // look for currency
            if (amount == null) {
                amount = parseAmount(numberFormats, token);
                if (amount != null) {
                    continue;
                }
            }
            if (description.length() > 0) {
                description.append(" ");
            }
            description.append(token);
        }

        if (amount != null) {
            int a = Math.round(amount.floatValue() * -100);
            try {
                manager.addEntry(a, description.toString());
            } catch (SQLException e) {
                Log.e(LOG_TAG, "database error", e);
                e.printStackTrace();
                throw new RuntimeException("database error", e);
            }
            // clear text field
            return true;
        }
        return false;
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
        addEntryTextView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (v.getId() == R.id.addEntry && KeyEvent.ACTION_DOWN == event.getAction()
                        && KeyEvent.KEYCODE_ENTER == keyCode) {
                    // disable tv
                    addEntryTextView.setEnabled(false);
                    // parse in bg
                    new OikosParseTask().execute(addEntryTextView.getText().toString());
                    return true; // eat the key
                }
                return false;
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
}
