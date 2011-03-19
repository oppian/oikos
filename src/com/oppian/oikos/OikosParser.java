package com.oppian.oikos;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import com.oppian.oikos.model.Entry;

public class OikosParser {
    
    private static NumberFormat DEFAULT_NUMBERFORMAT = NumberFormat.getNumberInstance(Locale.US);
    
    public static NumberFormat[] numberFormats() {
        return new NumberFormat[] {
                NumberFormat.getCurrencyInstance(),
                NumberFormat.getNumberInstance(),
                DEFAULT_NUMBERFORMAT
        };
    }
    
    public static final Entry parseEntry(String entryText, OikosManager manager) throws SQLException {
        // get number format
        NumberFormat[] numberFormats = numberFormats();
        StringBuilder description = new StringBuilder();
        Number amount = null;
        // tokenize string on whitespace
        String[] tokens = entryText.split("\\s");
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
            int a = numberToCurrencyInt(amount);
            return manager.addEntry(a, description.toString());
        }
        return null;
    }

    public static int numberToCurrencyInt(Number amount) {
        return Math.round(amount.floatValue() * 100) * -1;
    }
    
    public static Number parseAmount(NumberFormat[] numberFormats, String token) {
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
}
