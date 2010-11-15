package org.nick.wwwjdic.history;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.nick.wwwjdic.Analytics;
import org.nick.wwwjdic.Constants;
import org.nick.wwwjdic.DictionaryResultListView;
import org.nick.wwwjdic.ExamplesResultListView;
import org.nick.wwwjdic.KanjiResultListView;
import org.nick.wwwjdic.R;
import org.nick.wwwjdic.SearchCriteria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class SearchHistory extends HistoryBase {

    private static final String TAG = SearchHistory.class.getSimpleName();

    private static final String EXPORT_FILENAME = "wwwjdic/search-history.csv";

    protected void setupAdapter() {
        Cursor cursor = filterCursor();
        startManagingCursor(cursor);
        SearchHistoryAdapter adapter = new SearchHistoryAdapter(this, cursor);
        setListAdapter(adapter);
    }

    @Override
    protected void deleteAll() {
        Cursor c = filterCursor();

        db.beginTransaction();
        try {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex("_id"));
                db.deleteHistoryItem(id);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

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

        Analytics.event("lookupFromHistory", this);

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
    protected String getImportExportFilename() {
        File extStorage = Environment.getExternalStorageDirectory();

        return extStorage.getAbsolutePath() + "/" + EXPORT_FILENAME;
    }

    @Override
    protected void doExport(String filename) {
        CSVWriter writer = null;
        Cursor c = null;
        try {
            c = filterCursor();

            writer = new CSVWriter(new FileWriter(filename));

            int count = 0;
            while (c.moveToNext()) {
                long time = c.getLong(c.getColumnIndex("time"));
                SearchCriteria criteria = HistoryDbHelper.createCriteria(c);
                String[] criteriaStr = SearchCriteriaParser.toStringArray(
                        criteria, time);
                writer.writeNext(criteriaStr);
                count++;
            }

            Analytics.event("historyExport", this);

            String message = getResources()
                    .getString(R.string.history_exported);
            Toast t = Toast.makeText(this, String.format(message, filename,
                    count), Toast.LENGTH_SHORT);
            t.show();
        } catch (IOException e) {
            Log.e(TAG, "error exporting history", e);
            String message = getResources().getString(R.string.export_error);
            Toast.makeText(this, String.format(message, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "error closing CSV writer", e);
                }
            }
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    protected void doImport(String importFile) {
        CSVReader reader = null;

        db.beginTransaction();
        try {
            db.deleteAllHistory();

            reader = openImportFile(importFile);
            if (reader == null) {
                return;
            }

            String[] record = null;
            int count = 0;
            while ((record = reader.readNext()) != null) {
                SearchCriteria criteria = SearchCriteriaParser
                        .fromStringArray(record);
                long time = Long
                        .parseLong(record[SearchCriteriaParser.TIME_IDX]);
                db.addSearchCriteria(criteria, time);
                count++;
            }
            db.setTransactionSuccessful();

            refresh();

            Analytics.event("historyImport", this);

            String message = getResources()
                    .getString(R.string.history_imported);
            Toast t = Toast.makeText(this, String.format(message, importFile,
                    count), Toast.LENGTH_SHORT);
            t.show();
        } catch (IOException e) {
            Log.e(TAG, "error importing history", e);
            String message = getResources().getString(R.string.import_error);
            Toast.makeText(this, String.format(message, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "error closing CSV reader", e);
                }

            }
            db.endTransaction();
        }
    }

    @Override
    protected Cursor filterCursor() {
        if (selectedFilter == FILTER_ALL) {
            return db.getHistory();

        }

        return db.getHistoryByType(selectedFilter);
    }

    @Override
    protected String[] getFilterTypes() {
        return getResources().getStringArray(R.array.filter_types_history);
    }

}
