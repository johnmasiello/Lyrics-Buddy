package com.example.john.lyricsgrabber;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.john.lyricsgrabber", appContext.getPackageName());

        String lyrics = appContext.getString(R.string.lyrics);

        assertTrue("lyrics not read in", lyrics.length() > 0);

        LyricAnalyzer lyricAnalyzer = new LyricAnalyzer();

        Log.e("test", "----------------------------------ORIGINAL LYRICS---------------------------------------");
        Log.e("test", lyrics);

        String[] lines = lyrics.split("\\n");
        String[] trimmedLines = lyricAnalyzer.trimLyrics(lines);
        StringBuilder stringBuilder = new StringBuilder();

        for (String line : trimmedLines) {
            stringBuilder.append(line);
            stringBuilder.append('\n');

        }
        String trimmedLyrics = stringBuilder.toString();

        Log.e("test", "----------------------------------TRIMMED LYRICS---------------------------------------");
        Log.e("test", trimmedLyrics);
    }

    @Test
    public void printMatchingLineNumbers() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        String originalLyrics = appContext.getString(R.string.lyrics);
        LyricAnalyzer lyricAnalyzer = new LyricAnalyzer();

        // Find the matching lines
        String[] lines = originalLyrics.split("\\n");
        List<List<Integer>> matchingLineNumbers = lyricAnalyzer.findEquivalentLines(lines);

        // Find the color regions for the lines
        int[] regions = lyricAnalyzer.findColorRegions(matchingLineNumbers);

        // Construct readable output
        // consisting of the trimmed lyrics, and matching line numbers
        String[] trimmedLines = lyricAnalyzer.trimLyrics(lines);
        StringBuilder stringBuilder = new StringBuilder();
        int lineNo = 0;

        for (String line : trimmedLines) {
            stringBuilder.append(lineNo);
            stringBuilder.append("  ");
            stringBuilder.append(line);
            stringBuilder.append("  ");
            stringBuilder.append(lyricAnalyzer.printMatchingLineNumbers(
                    matchingLineNumbers.get(lineNo)));
            stringBuilder.append("  ");
            stringBuilder.append(printColorRegion(regions[lineNo]));
            stringBuilder.append('\n');

            lineNo++;
        }
        String trimmedLyrics = stringBuilder.toString();

        Log.e("test", "----------------------------------TRIMMED LYRICS---------------------------------------");
        Log.e("test", trimmedLyrics);
    }

    private char printColorRegion(int region) {
        return region > -1 ? (char) (65 + region) : ' ';
    }

    @Test
    public void testSplits() throws Exception {
        String src = "\n\na\n\n\nb";
        String[] splits = src.split("\\n");

        for (String split :
                splits) {

            System.out.println("split: " + (split != null ? split : "null"));

            /*
            Output:
            split:
            split: a
             */
        }

        String src_0 = "\na\n";
        assertEquals('a', src_0.charAt(1));
    }

    @Test
    public void testSplitKeepDelimiter() throws Exception {
        String src = System.lineSeparator()+System.lineSeparator()+"a"+
                     System.lineSeparator()+System.lineSeparator()+"b"; //"\n\na\n\n\nb";

        String regexTkn = System.lineSeparator();
        String[] splits = src.split("(?<="+regexTkn+")|(?="+regexTkn+")");

        for (String split :
                splits) {

            System.out.println("split: " + split.length() + (split.contains(System.lineSeparator()) ? "newline" : split));
        }
    }

    @Test
    public void testDelimiter() throws Exception {
        String src = "\na\n\nb\n\nc\n\t";

        LyricAnalyzer lyricAnalyzer = new LyricAnalyzer();
        String[] splits = lyricAnalyzer.delimitLines(src);

        for (String split : splits) {
            System.out.println("split: " + split);
        }
    }
}
