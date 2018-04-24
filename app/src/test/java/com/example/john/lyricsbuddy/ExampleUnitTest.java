package com.example.john.lyricsbuddy;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void newLineParser() throws Exception {
        String src = "x\n\nabcd\n";

//        System.out.println(String.valueOf(src.indexOf('\n', 3)));

        // Use a while-loop to traverse as delimiting over a single \n
        int count = 0;
        int index = 0;
        int nextIndex = src.indexOf('\n', 0);

        while (nextIndex != -1) {
            System.out.println(String.valueOf(++count) + ": length="+(nextIndex - index + 1) + " range="+(index) + ":" + (nextIndex));
            System.out.println(src.substring(index, nextIndex));

            index = ++nextIndex;
            nextIndex = src.indexOf('\n', index);
        }

        String[] splits = src.split("\\n");

        for (String split : splits) {
            System.out.println(split);
        }
    }

    @Test
    public void indexOf() throws Exception {
        int g = "77".indexOf("p", 2);
    }

    @Test
    public void filterContent() {
        String data = "Havana\n"+
                "Young Thug, Camila Cabello\n"+
                "Hey\n" +
                "\n" +
                "Havana, ooh na-na (ay)\n" +
                "Half of my heart is in Havana, ooh-na-na (ay, ay)\n" +
                "He took me back to East Atlanta, na-na-na\n" +
                "Oh, but my heart is in Havana (ay)\n" +
                "There's somethin' 'bout his manners (uh huh)\n" +
                "Havana, ooh na-na (uh)\n" +
                "\n" +
                "He didn't walk up with that \"how you doin'?\" (uh)\n" +
                "(When he came in the room)\n" +
                "He said there's a lot of girls I can do with (uh)\n" +
                "(But I can't without you)";


        // Parse the data using Analyzer
        LyricAnalyzer analyzer = new LyricAnalyzer();
        System.out.println("title: "+analyzer.fetchTitle(data));
        System.out.println("artist: "+analyzer.fetchArtist(data));
        System.out.println("lyrics: "+analyzer.fetchLyrics(data));
    }

    @Test
    public void sortedSet() {
        ArrayList<Long> longs = new ArrayList<>(Arrays.asList(3L, 2L, 1L));

        /*
        https://developer.android.com/reference/java/util/HashSet.html
            This class implements the Set interface, backed by a hash table (actually a HashMap instance).
            It makes no guarantees as to the iteration order of the set; in particular,
            it does not guarantee that the order will remain constant over time.
         */
        Set<Long> set = new HashSet<>(longs);

        for (Long val : set) {
            System.out.println(val);
        }


        SortedSet<Long> ordered = new TreeSet<>(longs);
        long previousVal = 0;

        for (Long val : ordered) {
             assertTrue(val > previousVal);
             previousVal = val;
        }
    }

    @Test
    public void callKotlinFromJava() {
        Integer p = null;
        assertEquals(0, LyricActionHelperKt.test(p));
    }
}