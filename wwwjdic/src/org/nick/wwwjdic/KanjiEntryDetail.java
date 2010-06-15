package org.nick.wwwjdic;

import java.util.ArrayList;
import java.util.List;

import org.nick.wwwjdic.sod.SodActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KanjiEntryDetail extends DetailActivity implements OnClickListener {

    private KanjiEntry entry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kanji_entry_details);

        entry = (KanjiEntry) getIntent().getSerializableExtra(
                Constants.KANJI_ENTRY_KEY);
        wwwjdicEntry = entry;
        isFavorite = getIntent().getBooleanExtra(Constants.IS_FAVORITE, false);

        setTitle(String.format("Details for '%s'", entry.getKanji()));

        TextView entryView = (TextView) findViewById(R.id.kanjiText);
        entryView.setText(entry.getKanji());
        entryView.setOnLongClickListener(this);

        TextView radicalGlyphText = (TextView) findViewById(R.id.radicalGlyphText);
        // radicalGlyphText.setTextSize(30f);
        Radicals radicals = Radicals.getInstance();
        Radical radical = radicals.getRadicalByNumber(entry.getRadicalNumber());
        radicalGlyphText.setText(radical.getGlyph().substring(0, 1));

        TextView radicalNumberView = (TextView) findViewById(R.id.radicalNumberText);
        radicalNumberView.setText(Integer.toString(entry.getRadicalNumber()));

        TextView strokeCountView = (TextView) findViewById(R.id.strokeCountText);
        strokeCountView.setText(Integer.toString(entry.getStrokeCount()));

        Button sodButton = (Button) findViewById(R.id.sod_button);
        sodButton.setOnClickListener(this);

        LinearLayout readingLayout = (LinearLayout) findViewById(R.id.readingLayout);

        if (entry.getReading() != null) {
            TextView onyomiView = new TextView(this, null,
                    R.style.dict_detail_reading);
            onyomiView.setText(entry.getOnyomi());
            readingLayout.addView(onyomiView);

            TextView kunyomiView = new TextView(this, null,
                    R.style.dict_detail_reading);
            kunyomiView.setText(entry.getKunyomi());
            readingLayout.addView(kunyomiView);
        }

        LinearLayout meaningsCodesLayout = (LinearLayout) findViewById(R.id.meaningsCodesLayout);

        if (entry.getMeanings().isEmpty()) {
            TextView text = new TextView(this, null,
                    R.style.dict_detail_meaning);
            meaningsCodesLayout.addView(text);
        } else {
            for (String meaning : entry.getMeanings()) {
                TextView text = new TextView(this, null,
                        R.style.dict_detail_meaning);
                text.setText(meaning);
                meaningsCodesLayout.addView(text);
            }
        }

        TextView moreLabel = new TextView(this);
        moreLabel.setText(R.string.codes_more);
        moreLabel.setTextColor(Color.WHITE);
        moreLabel.setBackgroundColor(Color.GRAY);
        meaningsCodesLayout.addView(moreLabel);

        List<Pair<String, String>> codesData = crieateCodesData(entry);
        for (Pair<String, String> codesEntry : codesData) {
            View codesEntryView = createLabelTextView(codesEntry);
            meaningsCodesLayout.addView(codesEntryView);
        }

        CheckBox starCb = (CheckBox) findViewById(R.id.star_kanji);
        starCb.setOnCheckedChangeListener(null);
        starCb.setChecked(isFavorite);
        starCb.setOnCheckedChangeListener(this);

        // ExpandableListView expandableList = new ExpandableListView(this);
        // KanjiCodesAdapter kanjiCodesAdapter = new KanjiCodesAdapter(entry);
        // expandableList.setAdapter(kanjiCodesAdapter);
        // // LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
        // // LayoutParams.FILL_PARENT);
        // // expandableList.setLayoutParams(lp);
        //
        // RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        // LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        // params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        // meaningsCodesLayout.addView(expandableList, params);

    }

    private List<Pair<String, String>> crieateCodesData(KanjiEntry entry) {
        ArrayList<Pair<String, String>> data = new ArrayList<Pair<String, String>>();
        if (entry.getJisCode() != null) {
            data.add(new Pair<String, String>(getStr(R.string.jis_code), entry
                    .getJisCode()));
        }

        if (entry.getUnicodeNumber() != null) {
            data.add(new Pair<String, String>(getStr(R.string.unicode_number),
                    entry.getUnicodeNumber()));
        }

        if (entry.getClassicalRadicalNumber() != null) {
            data.add(new Pair<String, String>(getStr(R.string.unicode_number),
                    entry.getClassicalRadicalNumber().toString()));
        }

        if (entry.getFrequncyeRank() != null) {
            data.add(new Pair<String, String>(getStr(R.string.freq_rank), entry
                    .getFrequncyeRank().toString()));
        }

        if (entry.getGrade() != null) {
            data.add(new Pair<String, String>(getStr(R.string.grade), entry
                    .getGrade().toString()));
        }

        if (entry.getJlptLevel() != null) {
            data.add(new Pair<String, String>(getStr(R.string.jlpt_level),
                    entry.getJlptLevel().toString()));
        }

        if (entry.getSkipCode() != null) {
            data.add(new Pair<String, String>(getStr(R.string.skip_code), entry
                    .getSkipCode()));
        }

        if (entry.getKoreanReading() != null) {
            data.add(new Pair<String, String>(getStr(R.string.korean_reading),
                    entry.getKoreanReading()));
        }

        if (entry.getPinyin() != null) {
            data.add(new Pair<String, String>(getStr(R.string.pinyn), entry
                    .getPinyin()));
        }

        return data;
    }

    private View createLabelTextView(Pair<String, String> data) {
        LinearLayout layout = new LinearLayout(KanjiEntryDetail.this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 5, 0);

        TextView labelView = new TextView(KanjiEntryDetail.this);
        labelView.setText(data.getFirst());
        labelView.setGravity(Gravity.LEFT);
        layout.addView(labelView, lp);

        TextView textView = new TextView(KanjiEntryDetail.this);
        textView.setText(data.getSecond());
        textView.setGravity(Gravity.RIGHT);
        layout.addView(textView, lp);

        return layout;
    }

    private String getStr(int id) {
        return getResources().getText(id).toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.sod_button:
            Intent intent = new Intent(this, SodActivity.class);
            intent.putExtra(Constants.KANJI_UNICODE_NUMBER, entry
                    .getUnicodeNumber());
            intent.putExtra(Constants.KANJI_GLYPH, entry.getKanji());

            startActivity(intent);
            break;
        default:
            // do nothing
        }
    }
}
