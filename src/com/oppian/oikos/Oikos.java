package com.oppian.oikos;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.app.ListActivity;
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
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager.SqliteOpenHelperFactory;
import com.oppian.oikos.adaptors.EntryAdapter;
import com.oppian.oikos.model.Entry;

public class Oikos extends ListActivity {

    private final String LOG_TAG = getClass().getSimpleName();

    static {
        OpenHelperManager.setOpenHelperFactory(new SqliteOpenHelperFactory() {
            public OrmLiteSqliteOpenHelper getHelper(Context context) {
                return new Db(context);
            }
        });
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "creating " + getClass());
        setContentView(R.layout.main);

        // 
        createMockEntries();
        // fillData();

        // get number format
        NumberFormat nf = NumberFormat.getCurrencyInstance();

        // set the list adaptor
        setListAdapter(new EntryAdapter(this, R.layout.list_item2, mEntryList, nf));

        ListView lv = getListView();

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        // setup add textview
        TextView tv = (TextView) findViewById(R.id.addEntry);
        tv.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i(LOG_TAG, event.toString() + " " + keyCode);
                if (v.getId() == R.id.addEntry && KeyEvent.ACTION_DOWN == event.getAction()
                        && KeyEvent.KEYCODE_ENTER == keyCode) {
                    TextView tv = (TextView) v;
                    parseEntry(tv.getText().toString());
                    return true; // eat the key
                }
                return false;
            }
        });
    }

    private void parseEntry(String text) {
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
            // check if we have a currency symbol
            // e.g "£ 2.20" or "2,20 €"
            if (amount == null) {
                if (x + 1 < tokens.length) {
                    // try parse this and the next token
                    StringBuilder sb = new StringBuilder(token.length() + tokens[x + 1].length() + 1);
                    sb.append(token).append(" ").append(tokens[x + 1]);
                    amount = parseAmount(numberFormats, sb.toString());
                    if (amount != null) {
                        x++; // skip
                        continue;
                    }
                }

                if (amount == null) {
                    // try parse one token
                    amount = parseAmount(numberFormats, token);
                    if (amount != null) {
                        continue;
                    }
                }
            }
            if (description.length() > 0) {
                description.append(" ");
            }
            description.append(token);
        }

        if (amount != null) {
            Entry entry = new Entry(amount.toString(), description.toString());
            Log.i("Entry", entry.toString());
        }
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

    private void fillData() {
        // Log.i(LOG_TAG, "fillData");
        // try {
        // Dao<Entry, Long> dao = getHelper().getEntryDao();
        // mEntryList = dao.queryForAll();
        // } catch (SQLException e) {
        // Log.e(LOG_TAG, "problem getting list", e);
        // }
    }

    private void createMockEntries() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("-2.30", "starbucks"));
        entries.add(new Entry("-1.50", "off license"));
        entries.add(new Entry("-45.30", "pub"));
        this.mEntryList = entries;
    }

    private List<Entry> mEntryList;

}
