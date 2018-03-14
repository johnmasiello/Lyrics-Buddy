package com.example.john.lyricsgrabber;

import org.junit.Test;

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
}