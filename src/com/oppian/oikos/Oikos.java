package com.oppian.oikos;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
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
import com.oppian.oikos.model.Account;
import com.oppian.oikos.model.Entry;

public class Oikos extends OrmLiteBaseListActivity<Db> {

    private final String LOG_TAG = getClass().getSimpleName();

    static {
        OpenHelperManager.setOpenHelperFactory(new SqliteOpenHelperFactory() {
            public OrmLiteSqliteOpenHelper getHelper(Context context) {
                return new Db(context);
            }
        });
    }

    private Account      account;

    private List<Entry>  entryList;
    
    private void fillData() {
        Log.i(LOG_TAG, "fillData");
        try {
            account = getHelper().getAccount();
            entryList = getHelper().entryList();
            updateAvg();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "problem getting list", e);
            throw new RuntimeException(e);
        }

        // get number format
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        
        // update total
        TextView totalTextView = (TextView)findViewById(R.id.total);
        totalTextView.setText(nf.format(account.getTotal()/100.0));

        // set the list adaptor
        setListAdapter(new EntryAdapter(this, R.layout.list_item, entryList, nf));
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "creating " + getClass());
        setContentView(R.layout.main);

        fillData();

        ListView lv = getListView();

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        // setup add textview
        TextView tv = (TextView) findViewById(R.id.addEntry);
        tv.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (v.getId() == R.id.addEntry && KeyEvent.ACTION_DOWN == event.getAction()
                        && KeyEvent.KEYCODE_ENTER == keyCode) {
                    TextView tv = (TextView) v;
                    if (parseEntry(tv.getText().toString())) {
                        tv.setText(""); // blank it
                    }
                    return true; // eat the key
                }
                return false;
            }
        });
    }

    private Number parseAmount(NumberFormat[] numberFormats, String token) {
        // try parse using currency
        for (NumberFormat numberFormat : numberFormats) {
            try {
                return numberFormat.parse(token);
            } catch (ParseException e) {

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
            Entry entry = new Entry(account, a, description.toString());
            Log.i(LOG_TAG, entry.toString());
            try {
                // create transaction entry
                getHelper().getEntryDao().create(entry);
                // update account
                account.setTotal(account.getTotal() + a);
                getHelper().getAccountDao().update(account);
                // redraw
                fillData();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "error with dao", e);
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void updateAvg() {
        int total = 0;
        // use the first entry date, or today if none
        Date firstDate = null;
        // iterate over entries
        for (Entry entry : entryList) {
            // only count negative entries
            if (entry.getAmount() < 0) {
                total = total + entry.getAmount();
                firstDate = entry.getEntryDate();
            }
        }
        if (firstDate == null) {
            firstDate = new Date();
        }
        Date now = new Date();
        long deltaDays = ((now.getTime() - firstDate.getTime()) / (24*60*60*1000) + 1); // inclusive
        TextView averageTextView = (TextView) findViewById(R.id.perday);
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        averageTextView.setText(nf.format((total / deltaDays)/100.0) + "/day");
    }

}
