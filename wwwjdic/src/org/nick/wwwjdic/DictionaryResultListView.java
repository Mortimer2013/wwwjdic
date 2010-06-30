package org.nick.wwwjdic;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class DictionaryResultListView extends
        ResultListViewBase<DictionaryEntry> {

    private List<DictionaryEntry> entries;

    public DictionaryResultListView() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_results);

        extractSearchCriteria();
        SearchTask<DictionaryEntry> searchTask = new DictionarySearchTask(
                getWwwjdicUrl(), getHttpTimeoutSeconds(), this, criteria);
        submitSearchTask(searchTask);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, DictionaryEntryDetail.class);
        DictionaryEntry entry = entries.get(position);
        intent.putExtra(Constants.ENTRY_KEY, entry);
        setFavoriteId(intent, entry);

        startActivity(intent);
    }

    public void setResult(final List<DictionaryEntry> result) {
        guiThread.post(new Runnable() {
            public void run() {
                entries = (List<DictionaryEntry>) result;
                DictionaryEntryAdapter adapter = new DictionaryEntryAdapter(
                        DictionaryResultListView.this, entries);
                setListAdapter(adapter);
                getListView().setTextFilterEnabled(true);
                setTitle(String.format("%d result(s) for '%s'", entries.size(),
                        criteria.getQueryString()));
                dismissProgressDialog();
            }
        });
    }

}
