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
import org.nick.wwwjdic.DictionaryEntry;

public class DictionaryEntryTest {

    @Test
    public void testParse() {
        String edictStr = "�搶�� [���񂹂�����] /(n) doctors/teachers/";
        DictionaryEntry entry = DictionaryEntry.parseEdict(edictStr);

        assertEquals("�搶��", entry.getWord());
        assertEquals("���񂹂�����", entry.getReading());
        assertEquals("(n)", entry.getPartOfSpeech());
        assertEquals(2, entry.getMeanings().size());
        assertEquals("doctors", entry.getMeanings().get(0));
        assertEquals("teachers", entry.getMeanings().get(1));
        assertEquals("[���񂹂�����]  (n) doctors teachers", entry
                .getTranslationString());
    }

    @Test
    public void testNoReading() {
        String edictStr = "�\���e�B�h�b�O /(n) salty dog (cocktail)/";
        DictionaryEntry entry = DictionaryEntry.parseEdict(edictStr);

        assertEquals("�\���e�B�h�b�O", entry.getWord());
        assertNull(entry.getReading());
        assertEquals("(n) salty dog (cocktail)", entry.getTranslationString());
        assertEquals("(n)", entry.getPartOfSpeech());
        assertEquals(1, entry.getMeanings().size());
        assertEquals("salty dog (cocktail)", entry.getMeanings().get(0));

    }

    @Test
    public void testEdict() throws Exception {
        FileInputStream fis = new FileInputStream(
                "C:/home/nick/android/wwwjdic-test/dict/edict");
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
            DictionaryEntry e = DictionaryEntry.parseEdict(l);
            assertNotNull(e);
            assertNotNull(e.getWord());
            assertNotNull(e.getTranslationString());
            assertNotNull(e.getShortTranslation());
        }

    }
}
