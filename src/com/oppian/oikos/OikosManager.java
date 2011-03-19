package com.oppian.oikos;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.oppian.oikos.model.Account;
import com.oppian.oikos.model.Entry;

/**
 * Manages the model data. Responsible for the business logic
 * and for getting data into and out of storage using DAO
 * 
 * @author Matthew Jacobi
 */
public class OikosManager {

    /**
     * The account object.
     */
    private Account               account;

    private Dao<Account, Integer> accountDao;

    private int                   average   = 0;

    private Dao<Entry, Integer>   entryDao;
    private List<Entry>           entryList;

    public OikosManager(Db db) throws SQLException {
        // setup daos
        entryDao = db.getEntryDao();
        accountDao = db.getAccountDao();
        // setup models
        account = loadAccount();
        entryList = db.entryList();
        average = calculateAverage();
    }

    /**
     * Creates a new entry and saves it.
     * 
     * @param amount The amount (in cents/pence/etc).
     * @param description The description for the entry
     * @throws SQLException If there was a SQL error
     */
    public void addEntry(int amount, String description) throws SQLException {
        // create entry
        Entry entry = new Entry(account, amount, description);
        // save the entry into the db
        entryDao.create(entry);
        // update the account
        synchronized (account) {
            account.setTotal(account.getTotal() + entry.getAmount());
            accountDao.update(account);
        }
        // add the item to the list
        synchronized (entryList) {
            entryList.add(0, entry);
        }
        // recalc the average
        average = calculateAverage();
    }

    public Account getAccount() {
        return account;
    }

    public int getAverage() {
        return average;
    }

    public List<Entry> getEntryList() {
        return Collections.unmodifiableList(entryList);
    }

    private int calculateAverage() {
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
        long deltaDays = ((now.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000) + 1); // inclusive
        return (int) (total / deltaDays);
    }

    private Account loadAccount() throws SQLException {
        // TODO: optimize to return one entry
        List<Account> list = accountDao.queryForAll();
        if (list.size() == 0) {
            // create
            Account account = new Account("Default", 0);
            accountDao.create(account);
            return account;
        }
        return list.get(0);
    }
}
