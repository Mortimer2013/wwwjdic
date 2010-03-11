package org.nick.wwwjdic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Wwwjdic extends TabActivity implements OnClickListener,
        OnFocusChangeListener, OnCheckedChangeListener {

    private EditText inputText;
    private CheckBox exactMatchCb;
    private CheckBox commonWordsCb;
    private CheckBox romanizedJapaneseCb;
    private Spinner dictSpinner;

    private EditText kanjiInputText;
    private Spinner kanjiSearchTypeSpinner;

    private TabHost tabHost;

    private static final Map<Integer, String> IDX_TO_DICT = new HashMap<Integer, String>();

    static {
        IDX_TO_DICT.put(0, "1");
        IDX_TO_DICT.put(1, "2");
        IDX_TO_DICT.put(2, "3");
        IDX_TO_DICT.put(3, "4");
        IDX_TO_DICT.put(4, "5");
        IDX_TO_DICT.put(5, "6");
        IDX_TO_DICT.put(6, "7");
        IDX_TO_DICT.put(7, "8");
        IDX_TO_DICT.put(8, "A");
        IDX_TO_DICT.put(9, "B");
        IDX_TO_DICT.put(10, "C");
        IDX_TO_DICT.put(11, "D");
    }

    private static final Map<Integer, String> IDX_TO_CODE = new HashMap<Integer, String>();

    static {
        // Kanji or reading
        IDX_TO_CODE.put(0, "J");
        // Stroke count
        IDX_TO_CODE.put(1, "C");
        // Radical number
        IDX_TO_CODE.put(2, "B");
        // English meaning
        IDX_TO_CODE.put(3, "E");
        // Unicode code (hex)
        IDX_TO_CODE.put(4, "U");
        // JIS code
        IDX_TO_CODE.put(5, "J");
        // SKIP code
        IDX_TO_CODE.put(6, "P");
        // Pinyin reading
        IDX_TO_CODE.put(7, "Y");
        // Korean reading
        IDX_TO_CODE.put(8, "W");
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("wordTab").setIndicator("Dictionary",
                getResources().getDrawable(android.R.drawable.ic_menu_search))
                .setContent(R.id.wordLookupTab));
        tabHost.addTab(tabHost.newTabSpec("kanjiTab").setIndicator("Kanji")
                .setContent(R.id.kanjiLookupTab));

        tabHost.setCurrentTab(0);

        findViews();

        View translateButton = findViewById(R.id.translateButton);
        translateButton.setOnClickListener(this);

        View kanjiSearchButton = findViewById(R.id.kanjiSearchButton);
        kanjiSearchButton.setOnClickListener(this);

        inputText.setOnFocusChangeListener(this);
        kanjiInputText.setOnFocusChangeListener(this);

        romanizedJapaneseCb.setOnCheckedChangeListener(this);
        exactMatchCb.setOnCheckedChangeListener(this);
        commonWordsCb.setOnCheckedChangeListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.dictinaries_array, R.layout.spinner_text);
        adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dictSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> kajiSearchTypeAdapter = ArrayAdapter
                .createFromResource(this, R.array.kanji_search_types_array,
                        R.layout.spinner_text);
        kajiSearchTypeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kanjiSearchTypeSpinner.setAdapter(kajiSearchTypeAdapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.translateButton:
            hideKeyboard();

            String input = inputText.getText().toString();

            try {
                int dictIdx = dictSpinner.getSelectedItemPosition();
                String dict = IDX_TO_DICT.get(dictIdx);
                Log.i("WWWJDIC", Integer.toString(dictIdx));
                Log.i("WWWJDIC", dict);
                if (dict == null) {
                    // edict
                    dict = "1";
                }

                SearchCriteria criteria = SearchCriteria.createForDictionary(
                        input, exactMatchCb.isChecked(), romanizedJapaneseCb
                                .isChecked(), commonWordsCb.isChecked(), dict);

                Intent intent = new Intent(this, DictionaryResultListView.class);
                intent.putExtra("org.nick.hello.searchCriteria", criteria);

                startActivity(intent);
            } catch (RejectedExecutionException e) {
                Log.e("WWWJDIC", "RejectedExecutionException", e);
            }
            break;
        case R.id.kanjiSearchButton:
            hideKeyboard();

            String kanjiInput = kanjiInputText.getText().toString();

            try {
                int searchTypeIdx = kanjiSearchTypeSpinner
                        .getSelectedItemPosition();
                String searchType = IDX_TO_CODE.get(searchTypeIdx);
                Log.i("WWWJDIC", Integer.toString(searchTypeIdx));
                Log.i("WWWJDIC", "kanji search type: " + searchType);
                if (searchType == null) {
                    // reading/kanji
                    searchType = "J";
                }

                SearchCriteria criteria = SearchCriteria.createForKanji(
                        kanjiInput, searchType);

                Intent intent = new Intent(this, KanjiResultListView.class);
                intent.putExtra("org.nick.hello.searchCriteria", criteria);

                startActivity(intent);
            } catch (RejectedExecutionException e) {
                Log.e("WWWJDIC", "RejectedExecutionException", e);
            }
            break;
        default:
            // do nothing
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
        case R.id.inputText:
            if (hasFocus) {
                showKeyboard();
            } else {
                hideKeyboard();
            }
            break;
        default:
            // do nothing
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "About").setIcon(
                android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case 1:
            showDialog(0);
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
        case 0:
            dialog = createAboutDialog();
            break;
        default:
            dialog = null;
        }

        return dialog;
    }

    private Dialog createAboutDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.about_dialog,
                (ViewGroup) findViewById(R.id.layout_root));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        AlertDialog alertDialog = builder.create();

        return alertDialog;
        //
        // Dialog dialog = new Dialog(this);
        //
        // dialog.setContentView(R.layout.about_dialog);
        // dialog.setTitle("About");
        //
        // return dialog;
    }

    private void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
    }

    private void showKeyboard() {
        EditText editText = (EditText) findViewById(R.id.inputText);
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void findViews() {
        inputText = (EditText) findViewById(R.id.inputText);
        exactMatchCb = (CheckBox) findViewById(R.id.exactMatchCb);
        commonWordsCb = (CheckBox) findViewById(R.id.commonWordsCb);
        romanizedJapaneseCb = (CheckBox) findViewById(R.id.romanizedCb);
        dictSpinner = (Spinner) findViewById(R.id.dictionarySpinner);
        kanjiInputText = (EditText) findViewById(R.id.kanjiInputText);
        kanjiSearchTypeSpinner = (Spinner) findViewById(R.id.kanjiSearchTypeSpinner);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
        case R.id.exactMatchCb:
            toggleRomanizedCb(isChecked);
            break;
        case R.id.commonWordsCb:
            toggleRomanizedCb(isChecked);
            break;
        case R.id.romanizedCb:
            toggleExactCommonCbs(isChecked);
            break;
        default:
            // do nothing
        }
    }

    private void toggleExactCommonCbs(boolean isChecked) {
        if (isChecked) {
            exactMatchCb.setEnabled(false);
            commonWordsCb.setEnabled(false);
        } else {
            exactMatchCb.setEnabled(true);
            commonWordsCb.setEnabled(true);
        }
    }

    private void toggleRomanizedCb(boolean isChecked) {
        if (isChecked) {
            romanizedJapaneseCb.setEnabled(false);
        } else {
            if (!exactMatchCb.isChecked() && !commonWordsCb.isChecked()) {
                romanizedJapaneseCb.setEnabled(true);
            }
        }
    }

}
