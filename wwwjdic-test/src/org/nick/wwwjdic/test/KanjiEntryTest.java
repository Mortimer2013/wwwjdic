package org.nick.wwwjdic.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nick.wwwjdic.KanjiEntry;

public class KanjiEntryTest {

    private static final String KANJIDIC_PATH = "C:/home/nick/android/wwwjdic/wwwjdic-test/dict/kanjidic";

    @Test
    public void testParse() {
        String kanjidicStr = "�� 3063 U9055 B162 G8 S13 S12 F344 J2 N4720 V6099 H3151 DK2014 L1644 K496 DO883 MN39067P MP11.0144 E1006 IN814 DF965 DC274 DJ385 DG713 DM1659 P3-3-10 I2q10.5 Q3430.4 DR1655 ZSP3-2-10 Ywei2 Wwi �C ����.�� ����.�� ����.���� -����.���� ����.�� ����.���� {difference} {differ} ";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("��", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(344, entry.getFrequncyeRank().intValue());
        assertEquals(8, entry.getGrade().intValue());
        assertEquals("3063", entry.getJisCode());
        assertEquals(2, entry.getJlptLevel().intValue());
        assertEquals("wi", entry.getKoreanReading());
        assertEquals("wei2", entry.getPinyin());
        assertEquals(162, entry.getRadicalNumber());
        assertEquals("3-3-10", entry.getSkipCode());
        assertEquals(13, entry.getStrokeCount());
        assertEquals("9055", entry.getUnicodeNumber());

        assertEquals("�C ����.�� ����.�� ����.���� -����.���� ����.�� ����.����", entry.getReading());
        assertEquals("�C", entry.getOnyomi());
        assertEquals("����.�� ����.�� ����.���� -����.���� ����.�� ����.����", entry.getKunyomi());
        assertEquals(2, entry.getMeanings().size());
        assertEquals("difference", entry.getMeanings().get(0));
        assertEquals("differ", entry.getMeanings().get(1));
    }

    @Test
    public void testParseNanori() {
        String kanjidicStr = "�� 3024 U963f B170 G9 S8 XN5008 F1126 J1 N4985 V6435 H346 DK256 L1295 K1515 O569 MN41599 MP11.0798 IN2258 DM1304 P1-3-5 I2d5.6 Q7122.0 Ya1 Ye1 Ya5 Ya2 Ya4 Wa Wog �A �I ������.�� ���� T1 �قƂ� ���� ���� ���� ���� �� {Africa} {flatter} {fawn upon} {corner} {nook} {recess}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("��", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(1126, entry.getFrequncyeRank().intValue());
        assertEquals(9, entry.getGrade().intValue());
        assertEquals("3024", entry.getJisCode());
        assertEquals(1, entry.getJlptLevel().intValue());
        // assertEquals("a og ", entry.getKoreanReading());
        // assertEquals("a1 e1 a5 a2 a4", entry.getPinyin());
        assertEquals(170, entry.getRadicalNumber());
        assertEquals("1-3-5", entry.getSkipCode());
        assertEquals(8, entry.getStrokeCount());
        assertEquals("963f", entry.getUnicodeNumber());

        assertEquals("�A �I ������.�� ���� �قƂ� ���� ���� ���� ���� ��", entry.getReading());
        assertEquals("�A �I", entry.getOnyomi());
        assertEquals("������.�� ����", entry.getKunyomi());
        assertEquals(6, entry.getMeanings().size());
        assertEquals("�قƂ� ���� ���� ���� ���� ��", entry.getNanori());
        assertEquals("Africa", entry.getMeanings().get(0));
        assertEquals("flatter", entry.getMeanings().get(1));
        assertEquals("fawn upon", entry.getMeanings().get(2));
        assertEquals("corner", entry.getMeanings().get(3));
        assertEquals("nook", entry.getMeanings().get(4));
        assertEquals("recess", entry.getMeanings().get(5));
    }

    @Test
    public void testParseNanoriNoKunyomi() {
        String kanjidicStr = "�� 303C U7d62 B120 G9 S12 F2315 J1 N3530 V4481 H1347 DK911 L2664 O2150 MN27427 MP8.1051 IN2194 P1-6-6 I6a6.14 Q2792.0 Yxuan4 Whyeon �P�� T1 ����� ���� {kimono design}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("��", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(2315, entry.getFrequncyeRank().intValue());
        assertEquals(9, entry.getGrade().intValue());
        assertEquals("303C", entry.getJisCode());
        assertEquals(1, entry.getJlptLevel().intValue());
        assertEquals("hyeon", entry.getKoreanReading());
        assertEquals("xuan4", entry.getPinyin());
        assertEquals(120, entry.getRadicalNumber());
        assertEquals("1-6-6", entry.getSkipCode());
        assertEquals(12, entry.getStrokeCount());
        assertEquals("7d62", entry.getUnicodeNumber());

        assertEquals("�P�� ����� ����", entry.getReading());
        assertEquals("�P��", entry.getOnyomi());
        assertEquals("", entry.getKunyomi());
        assertEquals("����� ����", entry.getNanori());
        assertEquals(1, entry.getMeanings().size());
        assertEquals("kimono design", entry.getMeanings().get(0));
    }

    @Test
    public void testParseNanoriLong() {
        String kanjidicStr = "�� 4038 U751f B100 G1 S5 F29 J4 N2991 V3715 H3497 DK2179 L1555 K29 O214 DO67 MN21670 MP7.1027 E42 IN44 DS34 DF71 DH44 DT43 DC9 DJ49 DB2.4 DG1327 DM1569 P4-5-2 I0a5.29 Q2510.0 DR2472 Ysheng1 Wsaeng �Z�C �V���E ��.���� ��.���� ��.���� ��.�܂�� ����.��� ��.�܂� ���܂� ��.�� ��.�� ��.���� ��.�₷ �� �Ȃ� �Ȃ�- ��.�� ��.�� ��.�� -�� T1 ���� ���� ���� ���� ���� ���܂� �� ���� ���イ ����� ���� �� ���傤 ���� �� ���� ���� �Ȃ� �ɂ� �ɂイ �� �� ���� �悢 ��イ {life} {genuine} {birth}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("��", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(29, entry.getFrequncyeRank().intValue());
        assertEquals(1, entry.getGrade().intValue());
        assertEquals("4038", entry.getJisCode());
        assertEquals(4, entry.getJlptLevel().intValue());
        assertEquals("saeng", entry.getKoreanReading());
        assertEquals("sheng1", entry.getPinyin());
        assertEquals(100, entry.getRadicalNumber());
        assertEquals("4-5-2", entry.getSkipCode());
        assertEquals(5, entry.getStrokeCount());
        assertEquals("751f", entry.getUnicodeNumber());
        assertEquals(
                "�Z�C �V���E ��.���� ��.���� ��.���� ��.�܂�� ����.��� ��.�܂� ���܂� ��.�� ��.�� ��.���� ��.�₷ �� �Ȃ� �Ȃ�- ��.�� ��.�� ��.�� -�� ���� ���� ���� ���� ���� ���܂� �� ���� ���イ ����� ���� �� ���傤 ���� �� ���� ���� �Ȃ� �ɂ� �ɂイ �� �� ���� �悢 ��イ",
                entry.getReading());
        assertEquals("�Z�C �V���E", entry.getOnyomi());
        assertEquals(
                "��.���� ��.���� ��.���� ��.�܂�� ����.��� ��.�܂� ���܂� ��.�� ��.�� ��.���� ��.�₷ �� �Ȃ� �Ȃ�- ��.�� ��.�� ��.�� -��",
                entry.getKunyomi());
        assertEquals(
                "���� ���� ���� ���� ���� ���܂� �� ���� ���イ ����� ���� �� ���傤 ���� �� ���� ���� �Ȃ� �ɂ� �ɂイ �� �� ���� �悢 ��イ",
                entry.getNanori());
        assertEquals(3, entry.getMeanings().size());
        assertEquals("life", entry.getMeanings().get(0));
        assertEquals("genuine", entry.getMeanings().get(1));
        assertEquals("birth", entry.getMeanings().get(2));
    }

    @Test
    public void testKanjidic() throws Exception {
        FileInputStream fis = new FileInputStream(KANJIDIC_PATH);
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(new InputStreamReader(fis,
                "EUC-JP"));
        String line = null;

        while ((line = r.readLine()) != null) {
            lines.add(line);
        }

        r.close();

        for (String l : lines) {
            if (l.charAt(0) == '#') {
                continue;
            }

            System.out.println("parsing : " + l);
            KanjiEntry entry = KanjiEntry.parseKanjidic(l);
            assertNotNull(entry);
            assertNotNull(entry.getKanji());
            assertNotNull(entry.getReading());
            // assertFalse(entry.getMeanings().isEmpty());
        }

    }
}
