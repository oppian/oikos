package com.oppian.oikos;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.oppian.oikos.model.Account;
import com.oppian.oikos.model.Entry;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class Db extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application
    private static final String DATABASE_NAME    = "oikos.db";
    private static final int    DATABASE_VERSION = 8;

    // the daos
    private Dao<Entry, Integer>    entryDao        = null;
    private Dao<Account, Integer>    accountDao        = null;

    public Db(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        Log.i(Db.class.getName(), "onCreate");
        try {
            TableUtils.createTable(connectionSource, Entry.class);
            TableUtils.createTable(connectionSource, Account.class);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(Db.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(Db.class.getName(), "onUpgrade");
        try {
            TableUtils.dropTable(connectionSource, Entry.class, true);
            TableUtils.dropTable(connectionSource, Account.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(Db.class.getName(), "Can't drop database", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Entry, Integer> getEntryDao() throws SQLException {
        if (entryDao == null) {
            entryDao = BaseDaoImpl.createDao(getConnectionSource(), Entry.class);
        }
        return entryDao;
    }
    
    public List<Entry> entryList() throws SQLException {
        Dao<Entry, Integer> dao = getEntryDao();
        return dao.query(dao.queryBuilder()
                .orderBy("id", false)
                .prepare());
    }

    @Override
    public void close() {
        super.close();
        entryDao = null;
    }

    public Dao<Account, Integer> getAccountDao() throws SQLException {
        if (accountDao == null) {
            accountDao = BaseDaoImpl.createDao(getConnectionSource(), Account.class);
        }
        return accountDao;
    }
    
    public Account getAccount() throws SQLException {
        Dao<Account, Integer> dao = getAccountDao();
        List<Account> list = dao.queryForAll();
        if (list.size() == 0) {
            // create
            Account account = new Account("Default", 0);
            dao.create(account);
            return account;
        }
        return list.get(0);
            
    }

}
