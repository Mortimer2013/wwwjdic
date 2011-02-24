package org.nick.wwwjdic.krad;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nick.wwwjdic.Constants;
import org.nick.wwwjdic.R;
import org.nick.wwwjdic.hkr.HkrCandidates;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class KradChart extends Activity implements OnClickListener,
        OnItemClickListener {

    private static final String TAG = KradChart.class.getSimpleName();

    private static final int NUM_KRAD_RADICALS = 252;
    private static final List<String> NUM_STROKES = Arrays.asList(new String[] {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
            "13", "14", "17" });

    private static final String HAT = "𠆢";
    private static final Map<Character, Character> KRAD_TO_DISPLAY = new HashMap<Character, Character>();
    static {
        KRAD_TO_DISPLAY.put('⺅', '亻');
        KRAD_TO_DISPLAY.put('⺾', '艹');
        KRAD_TO_DISPLAY.put('辶', '辶');
        KRAD_TO_DISPLAY.put('⻏', '邦');
        KRAD_TO_DISPLAY.put('⻖', '阡');
        KRAD_TO_DISPLAY.put('⺌', '尚');
        KRAD_TO_DISPLAY.put(HAT.charAt(0), '个');
        KRAD_TO_DISPLAY.put('⺹', '耂');
    }
    private static final List<String> REPLACED_CHARS = Arrays
            .asList(new String[] { "邦", "阡", "尚", "个" });

    private List<String> radicals = new ArrayList<String>();
    private Set<Character> selectedRadicals = new HashSet<Character>();
    private Set<Character> enabledRadicals = new HashSet<Character>();
    private Set<Character> matchingKanjis;

    private TextView matchedKanji;
    private GridView radicalChartGrid;
    private KradAdapter adapter;

    private KradDb kradDb = new KradDb();

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.krad_chart);

        matchedKanji = (TextView) findViewById(R.id.matched_kanji);
        matchedKanji.setOnClickListener(this);
        radicalChartGrid = (GridView) findViewById(R.id.kradChartGrid);
        radicalChartGrid.setOnItemClickListener(this);

        setTitle(R.string.select_radical);

        new AsyncTask<Void, Void, Boolean>() {

            private Throwable error;

            @Override
            protected void onPreExecute() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();

                }
                progressDialog = new ProgressDialog(KradChart.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    try {
                        initKradDb();

                        for (String numStrokesStr : NUM_STROKES) {
                            String labelStr = new String(numStrokesStr);
                            radicals.add(labelStr);

                            String arrayName = "_" + numStrokesStr + "_stroke";
                            Field field = R.array.class.getField(arrayName);
                            int resourceId = (Integer) field.get(null);
                            String[] radicalArr = getResources()
                                    .getStringArray(resourceId);
                            radicals.addAll(Arrays.asList(radicalArr));
                        }
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                } catch (Exception e) {
                    error = e;
                    Log.d(TAG, "Error loading radkfile-u", e);

                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();

                    if (result) {
                        enableAllRadicals();
                        adapter = new KradAdapter(KradChart.this,
                                R.layout.krad_item, radicals);
                        radicalChartGrid.setAdapter(adapter);
                    } else {
                        Toast t = Toast.makeText(
                                KradChart.this,
                                "error loading radkfile-u "
                                        + error.getMessage(),
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            }
        }.execute();
    }

    private void initKradDb() {
        if (!kradDb.isInitialized()) {
            try {
                InputStream in = getAssets().open("radkfile-u");
                kradDb.readFromStream(in);
            } catch (IOException e) {
                Log.e(TAG, "error reading radkfile-u", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void enableAllRadicals() {
        for (String radical : radicals) {
            if (!isStrokeNumLabel(radical)) {
                enabledRadicals.add(radical.trim().charAt(0));
            }
        }
    }

    public class KradAdapter extends ArrayAdapter<String> {

        public KradAdapter(Context context, int textViewResourceId,
                List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            TextView result = (TextView) super.getView(position, convertView,
                    viewGroup);
            result.setTextColor(Color.WHITE);
            result.setBackgroundColor(Color.TRANSPARENT);
            result.setTextSize(24f);

            String modelStr = getItem(position);
            if (isStrokeNumLabel(modelStr)) {
                result.setBackgroundColor(Color.GRAY);
            } else {
                Character radical = modelStr.trim().charAt(0);
                String displayStr = toDisplayStr(radical);
                if (REPLACED_CHARS.contains(displayStr)) {
                    result.setText(displayStr);
                    result.setTextColor(Color.LTGRAY);
                }
            }

            if (isSelected(modelStr)) {
                result.setBackgroundColor(Color.GREEN);
            }

            if (isDisabled(modelStr)) {
                result.setBackgroundColor(Color.DKGRAY);
            }

            return result;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            String text = getItem(position);
            return !isStrokeNumLabel(text) && !isDisabled(text);
        }
    }

    private static boolean isStrokeNumLabel(String str) {
        return NUM_STROKES.contains(str.trim());
    }

    private boolean isSelected(String radicalStr) {
        Character radical = radicalStr.trim().charAt(0);
        return selectedRadicals.contains(radical);
    }

    private boolean isDisabled(String radicalStr) {
        Character radical = radicalStr.trim().charAt(0);
        return !isStrokeNumLabel(radicalStr)
                && !enabledRadicals.contains(radical);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Character radical = radicals.get(position).trim().charAt(0);
        if (selectedRadicals.contains(radical)) {
            selectedRadicals.remove(radical);
        } else {
            selectedRadicals.add(radical);
        }

        if (selectedRadicals.isEmpty()) {
            enableAllRadicals();
            matchedKanji.setText("");
        } else {
            matchingKanjis = kradDb.getKanjisForRadicals(selectedRadicals);
            String matchingKanjisStr = TextUtils.join("", matchingKanjis);
            matchedKanji.setText(matchingKanjisStr);

            Log.d(TAG, "matching kanjis: " + matchingKanjis);
            enabledRadicals = kradDb.getRadicalsForKanjis(matchingKanjis);
            Log.d(TAG, "enabled radicals: " + enabledRadicals);
        }

        adapter.notifyDataSetChanged();
    }

    private static String toDisplayStr(Character radical) {
        Character displayChar = KRAD_TO_DISPLAY.get(radical);
        if (displayChar == null) {
            displayChar = radical;
        }

        if (displayChar != radical) {
            Log.d(TAG, String.format("%s %s", radical, displayChar));
        }
        return Character.toString(displayChar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.matched_kanji:
            String[] kanji = new String[matchingKanjis.size()];
            int i = 0;
            for (Character c : matchingKanjis) {
                kanji[i] = Character.toString(c);
                i++;
            }
            Intent intent = new Intent(this, HkrCandidates.class);
            intent.putExtra(Constants.HKR_CANDIDATES_KEY, kanji);
            startActivity(intent);
            break;
        default:
            // do nothing
        }
    }
}
