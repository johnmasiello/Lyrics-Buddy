package com.example.john.lyricsgrabber;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 3/7/18.
 * A utility class that analyzes the structure of lyrics to differentiate verse from chorus
 */

class LyricAnalyzer {
    /**
     *
     * @return a copy of lines where each lines is modified by trimming white space, removing punctuation
     */
    String[] trimLyrics(@NonNull String[] lines) {
        String[] out = new String[lines.length];

        int i = 0;
        for (String line : lines) {
            out[i++] = line.replaceAll("[^A-Za-z0-9-' ]", " ").replaceAll(" +", " ").trim();
        }

        return out;
    }

    /**
     *
     * @param lyricLine Precondition: trimLyrics
     * @see #trimLyrics(String[])
     */
    private boolean containsWords_(String lyricLine) {
        return lyricLine.length() > 0;
    }

    boolean containsWords(String lyricLine) { return lyricLine.trim().length() > 0; }

    /**
     * @param lines the original lines of the lyrics
     * @return an list of List{@literal <Integer>} where each element of inner list is an
     * index i corresponding to a 'matching' line of lyrics to to line lines[index], i != index, where
     * 'matching' lines are given by String.equalsIgnoreCase(x) where x is a line obtained by trimLyrics(lines)
     * @see #trimLyrics(String[])
     */
    List<List<Integer>> findEquivalentLines(@NonNull String[] lines) {

        String[] tLines = trimLyrics(lines);
        String line;
        List<List<Integer>> equivalentLines = new ArrayList<>(tLines.length);
        List<Integer> list;

        for (int i = 0; i < tLines.length; i++) {
            line = tLines[i];

            if (containsWords_(line)) {
                list = new ArrayList<>();

                for (int j = 0; j < tLines.length; j++) {
                    if (j != i && line.equalsIgnoreCase(tLines[j]))
                        list.add(j);
                }
            } else {
                list = null;
            }
            equivalentLines.add(list);
        }

        return equivalentLines;
    }

    /**
     *
     * @param matchingLineNumbers matching line numbers using 0-based counting
     * @return human-readable output of matchLineNumbers
     */
    String printMatchingLineNumbers(List<Integer> matchingLineNumbers) {

        if (matchingLineNumbers == null) {
            return "";
        }

        StringBuilder out = new StringBuilder("");

        for (Integer i : matchingLineNumbers) {
            out.append(i);
            out.append(' ');
        }

        return out.toString();
    }

    /**
     *
     * @param equivalentLines contains the line numbers of matching lines
     * @return regions based on stanzas with lines matching other lines in the lyrics. A value
     * of 0 means to use a default or system color, indicating a verse, which should consist
     * of lines distinct from the rest of the verse. Values greater than 0 indicate a line or
     * region that is repeating throughout the underlying lyrics.
     */
    int[] findColorRegions(List<List<Integer>> equivalentLines) {

        int[] regions = new int[equivalentLines.size()];

        // Initialize to a value indicating uninitialized
        for (int i = 0; i < regions.length; i++) {
            regions[i] = -1;
        }

        List<Integer> matches;
        boolean newStanza;
        int coerceColor, lineColor, verseColor;
        int previousIndex, index;

        lineColor = 0;
        previousIndex = index = 0;
        coerceColor = -1;
        newStanza = true;

        while (index < regions.length) {
            matches = equivalentLines.get(index);

            if (matches == null || matches.size() > 0) {

                // Case a line is repeating with some other line in the lyrics
                 if (matches != null) {
                    if (index > matches.get(0)) {
                        regions[index] = regions[matches.get(0)];
                    } else if (newStanza) {
                        regions[index] = ++lineColor; // Bump the color, once, on new stanza
                        newStanza = false;
                    } else {
                        regions[index] = lineColor;
                    }
                    coerceColor = lineColor;

                }

                // Set the color region for non-repeating lines within the stanza
                if (previousIndex < index) {
                    verseColor = index - previousIndex > 2 || coerceColor == -1 ? 0 : coerceColor;
                    do {
                        regions[previousIndex++] = verseColor;
                    } while (previousIndex < index);
                }

                // Case end of current stanza
                if (matches == null) {
                    coerceColor = -1;
                    newStanza = true;
                }

            } else { // Case a line is distinct from all other lines in the lyrics
                index++;
                continue;
            }

            // Post-updates
            previousIndex = ++index;
        }

        // Set the color region for non-repeating lines within the last stanza
        if (previousIndex < index) {
            verseColor = index - previousIndex > 2 || coerceColor == -1 ? 0 : coerceColor;
            do {
                regions[previousIndex++] = verseColor;
            } while (previousIndex < index);
        }
        return regions;
    }

    /**
     *
     * @param body The text to split
     * @return Splits with the delimiter D=System.separator() as separate splits from the non-delimited expressions
     */
    String[] delimitLines(String body) {
        String regexTkn;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            regexTkn = System.lineSeparator();
        } else {
            // Assume UNIX system
            regexTkn = "\n";
        }

        // Split on the regex: {LookAhead{token}}|{LookBehind{token}}
        return body.split("(?<="+regexTkn+")|(?="+regexTkn+")");

    }
}
