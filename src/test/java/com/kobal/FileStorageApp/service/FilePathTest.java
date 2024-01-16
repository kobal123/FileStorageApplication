package com.kobal.FileStorageApp.service;

import com.kobal.FileStorageApp.file.service.FilePath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilePathTest {


    @Test
    void testAddPart() {
        //given
        FilePath underTest = new FilePath();
        String expectedPath = "/some/%20/path";
        int expectedSize = 3;

        //when
        String actual = underTest.addPartEncoded("some")
                .addPartEncoded(" ")
                .addPartEncoded("path")
                .addPartEncoded("")
                .addPartEncoded("/")
                .addPartEncoded(null)
                .toString();

        // then
        assertFalse(actual.contains("+"));
        assertEquals(expectedSize, underTest.getSize());
        assertEquals(actual, expectedPath);
    }
    @Test
    void testConstructorWithPathInput() {
        //given
        FilePath underTest = new FilePath("some/ /path");
        String expectedPath = "/some/%20/path";

        //when
        String actual = underTest.toString();

        // then
        assertEquals(actual, expectedPath);
    }


    @Test
    void getParent() {
        //given
        FilePath underTest = new FilePath();
        String expectedParentPath = "/some/%20";

        //when
        String actual = underTest.addPartEncoded("some")
                .addPartEncoded(" ")
                .addPartEncoded("path")
                .getParent()
                .toString();

        //then
        assertEquals(expectedParentPath, actual);
    }

    @Test
    void getSegment() {
        //given
        FilePath underTest = new FilePath();
        String expected = "to"; // the URLEncoder encodes spaces as +

        //when
        String actual = underTest.addPartEncoded("some")
                .addPartEncoded("path")
                .addPartEncoded(expected)
                .addPartEncoded("a")
                .addPartEncoded("file")
                .getSegment(2);


        //then
        assertEquals(expected, actual);
    }

    @Test
    void getFileName() {
        //given
        FilePath underTest = new FilePath();
        String expectedFileName = "file.txt";

        //when
        String actual = underTest.addPartEncoded("some")
                .addPartEncoded("path")
                .addPartEncoded(expectedFileName)
                .getFileName();

        // then
        assertEquals(expectedFileName, actual);
    }


    @Test
    void addPartRawCopy() {
        // given
        FilePath filePath = new FilePath("abc");

        // when
        FilePath copy = filePath.addPartRawCopy(" ");

        // then
        assertNotSame(filePath, copy);
        assertEquals(copy.toString(), "/abc/ ");
        assertEquals(filePath.toString(), "/abc");
    }

    @Test
    void ofRaw() {
        String expected = "/";
        String actual = FilePath.ofRaw(null);
        assertEquals(expected, actual);
    }

}