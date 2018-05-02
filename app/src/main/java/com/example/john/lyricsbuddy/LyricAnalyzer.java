package com.example.john.lyricsbuddy;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 3/7/18.
 * A utility class that analyzes the structure of lyrics to differentiate verse from chorus
 */

class LyricAnalyzer {

    private int lyricParsingOffset;

    /**
     *
     * @return a copy of lines where each lines is modified by trimming white space, removing punctuation
     */
    String[] trimLyrics(@NonNull String[] lines) {
        String[] out = new String[lines.length];

        int i = 0;
        for (String line : lines) {
            out[i++] = line.replaceAll("[\\x00-\\x20,.;:!?()\\[\\]{}\"]", " ").replaceAll(" +", " ").trim();
        }

        return out;
    }

    private boolean containsWordsNoTrim(String lyricLine) {
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
        List<Integer> listMatchingLine_Numbers;

        for (int i = 0; i < tLines.length; i++) {
            line = tLines[i];

            if (containsWordsNoTrim(line)) {
                listMatchingLine_Numbers = new ArrayList<>();

                for (int j = 0; j < tLines.length; j++) {
                    if (j != i && line.equalsIgnoreCase(tLines[j]))
                        listMatchingLine_Numbers.add(j);
                }
            } else {
                listMatchingLine_Numbers = null;
            }
            equivalentLines.add(listMatchingLine_Numbers);
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

        List<Integer> lineMatches;
        boolean newStanza;
        int coerceColor, lineColor, verseColor;
        int previousIndex, index;

        lineColor = 0;
        previousIndex = index = 0;
        coerceColor = -1;
        newStanza = true;

        while (index < regions.length) {
            lineMatches = equivalentLines.get(index);

            if (lineMatches == null || lineMatches.size() > 0) {

                // Case a line is repeating with some other line in the lyrics
                 if (lineMatches != null) {
                    if (index > lineMatches.get(0)) {
                        coerceColor = regions[index] = regions[lineMatches.get(0)];
                    } else if (newStanza) {
                        coerceColor = regions[index] = ++lineColor; // Bump the color, once, on new stanza
                        newStanza = false;
                    } else {
                        coerceColor = regions[index] = lineColor;
                    }
                }

                // Set the color region for non-repeating lines within the stanza
                if (previousIndex < index) {
                    verseColor = index - previousIndex > 2 || coerceColor == -1 ? 0 : coerceColor;
                    do {
                        regions[previousIndex++] = verseColor;
                    } while (previousIndex < index);
                }

                // Case end of current stanza
                if (lineMatches == null) {
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
     * @return Splits with the delimiter D='\\n'
     */
    String[] delimitLines(String body) {
        return body.split("\\n");
    }

    /**
     *
     * @see #fetchLyrics(String)
     */
    String fetchTitle(String raw) {
        return findNextLetteredLine(raw, 0);
    }

    /**
     *
     * @see #fetchLyrics(String)
     */
    String fetchArtist(String raw) {
        fetchTitle(raw);
        return findNextLetteredLine(raw, ++lyricParsingOffset);
    }

    /**
     *
     * <p>Related Methods {@link #fetchTitle(String)}, {@link #fetchArtist(String)}</p>
     * @param raw The raw text in the form {Title}\n{Artist}\n{Body}
     * @return The lyric body of raw
     */
    String fetchLyrics(String raw) {
        fetchArtist(raw);
        lyricParsingOffset++;
        return lyricParsingOffset < raw.length() ? raw.substring(lyricParsingOffset) : "";
    }

    private String findNextLetteredLine(String raw, int startOffset) {
        int endOffset;
        String parsed = "";

        final String terminal = "\n";
        endOffset = raw.indexOf(terminal, startOffset);

        while (endOffset != -1) {
            parsed = raw.substring(startOffset, endOffset);

            if (containsWords(parsed)) {
                lyricParsingOffset = endOffset;
                return parsed;
            }
            startOffset = ++endOffset;
            endOffset = raw.indexOf(terminal, startOffset);
        }
        endOffset = raw.length();

        if (startOffset < endOffset) {
            parsed = raw.substring(startOffset, endOffset);

            if (!containsWords(parsed)) {
                parsed = "";
            }
        }
        lyricParsingOffset = endOffset;
        return parsed;
    }
}
