package org.nick.wwwjdic.history;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.nick.wwwjdic.Constants;
import org.nick.wwwjdic.DictionaryResultListView;
import org.nick.wwwjdic.ExamplesResultListView;
import org.nick.wwwjdic.KanjiResultListView;
import org.nick.wwwjdic.R;
import org.nick.wwwjdic.SearchCriteria;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class SearchHistory extends HistoryBase {

    private static final String TAG = SearchHistory.class.getSimpleName();

    protected void setupAdapter() {
        Cursor cursor = db.getHistory();
        startManagingCursor(cursor);
        SearchHistoryAdapter adapter = new SearchHistoryAdapter(this, cursor);
        setListAdapter(adapter);
    }

    @Override
    protected void deleteAll() {
        db.deleteAllHistory();
        refresh();
    }

    @Override
    protected int getContentView() {
        return R.layout.search_history;
    }

    @Override
    protected void lookupCurrentItem() {
        SearchCriteria criteria = getCurrentCriteria();

        Intent intent = null;
        switch (criteria.getType()) {
        case SearchCriteria.CRITERIA_TYPE_DICT:
            intent = new Intent(this, DictionaryResultListView.class);
            break;
        case SearchCriteria.CRITERIA_TYPE_KANJI:
            intent = new Intent(this, KanjiResultListView.class);
            break;
        case SearchCriteria.CRITERIA_TYPE_EXAMPLES:
            intent = new Intent(this, ExamplesResultListView.class);
            break;
        default:
            // do nothing?
        }

        intent.putExtra(Constants.CRITERIA_KEY, criteria);

        startActivity(intent);
    }

    @Override
    protected void deleteCurrentItem() {
        Cursor c = getCursor();
        int idx = c.getColumnIndex("_id");
        int id = c.getInt(idx);
        db.deleteHistoryItem(id);

        refresh();
    }

    @Override
    protected void copyCurrentItem() {
        SearchCriteria criteria = getCurrentCriteria();
        clipboardManager.setText(criteria.getQueryString());
    }

    private SearchCriteria getCurrentCriteria() {
        Cursor c = getCursor();
        SearchCriteria criteria = HistoryDbHelper.createCriteria(c);
        return criteria;
    }

    @Override
    protected void exportItems() {
        CSVWriter writer = null;

        try {
            Cursor c = db.getHistory();
            File extStorage = Environment.getExternalStorageDirectory();
            String exportFile = extStorage.getAbsolutePath()
                    + "/search-history.csv";
            writer = new CSVWriter(new FileWriter(exportFile));

            while (c.moveToNext()) {
                long time = c.getLong(c.getColumnIndex("time"));
                SearchCriteria criteria = HistoryDbHelper.createCriteria(c);
                String[] criteriaStr = SearchCriteriaParser.toStringArray(
                        criteria, time);
                writer.writeNext(criteriaStr);

            }

            String message = getResources()
                    .getString(R.string.history_exported);
            Toast t = Toast.makeText(this, String.format(message, exportFile),
                    Toast.LENGTH_SHORT);
            t.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "error closing CSV writer", e);
                }

            }
        }
    }

    @Override
    protected void importItems() {
        CSVReader reader = null;

        SQLiteDatabase s = db.getWritableDatabase();
        s.beginTransaction();
        try {
            File extStorage = Environment.getExternalStorageDirectory();
            String importFile = extStorage.getAbsolutePath()
                    + "/search-history.csv";
            reader = new CSVReader(new FileReader(importFile));

            String[] record = null;
            while ((record = reader.readNext()) != null) {
                SearchCriteria criteria = SearchCriteriaParser
                        .fromStringArray(record);
                long time = Long.parseLong(record[11]);
                db.addSearchCriteria(criteria, time);

            }
            s.setTransactionSuccessful();

            refresh();

            String message = getResources()
                    .getString(R.string.history_imported);
            Toast t = Toast.makeText(this, String.format(message, importFile),
                    Toast.LENGTH_SHORT);
            t.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "error closing CSV reader", e);
                }

            }
            s.endTransaction();
        }
    }

    @Override
    protected void filter(int type) {
        Cursor c = null;
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        if (type == FILTER_ALL) {
            c = db.getHistory();

        } else {
            c = db.getHistoryByType(type);
        }

        adapter.changeCursor(c);
        refresh();
    }

}
