package com.kobal.FileStorageApp.file.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FilePath {
    private final List<String> segments = new ArrayList<>();

    public FilePath() {}

    public FilePath(String path) {
        addPartEncoded(path);
    }

    public static FilePath encode(String s) {
        return new FilePath().addPartEncoded(s);
    }

    public static FilePath decode(String s) {
        return new FilePath().addPartEncoded(s);
    }

    public static FilePath raw(String s) {
        return new FilePath().addPartRaw(s);
    }

    public static String ofRaw(String s) {
        StringJoiner joiner = new StringJoiner("/","/","");
        if (s == null)
            return joiner.toString();

        String[] parts = s.split("/");
        for (String part : parts) {
            if (part.length() != 0)
                joiner.add(part);
        }
        return joiner.toString();
    }

    public FilePath addPartEncoded(String s) {
        if (s == null || s.length() == 0)
            return this;
        String[] parts = s.split("/");
        for (String part : parts) {
            if (part.length() != 0)
                segments.add(URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return this;
    }

    public FilePath addPartDecoded(String s) {
        if (s == null || s.length() == 0)
            return this;
        String[] parts = s.split("/");
        for (String part : parts) {
            if (part.length() != 0)
                segments.add(URLDecoder.decode(part.replace("%20", "+"), StandardCharsets.UTF_8));
        }
        return this;
    }

    public FilePath addPartRaw(String s) {
        if (s == null || s.length() == 0)
            return this;
        String[] parts = s.split("/");
        for (String part : parts) {
            if (part.length() != 0) {
                segments.add(part);
            }
        }
        return this;
    }

    public FilePath addPartRaw(FilePath filePath) {
        return addPartRaw(filePath.toString());
    }


    public FilePath addPartRawCopy(String s) {
        return new FilePath()
                .addPartRaw(String.join("/",segments))
                .addPartRaw(s);
    }
    public FilePath addPartRawCopy(FilePath filePath) {
        return addPartRawCopy(filePath.toString());
    }


    public FilePath getParent() {
        FilePath parent = new FilePath();
        for (int i = 0; i < segments.size() - 1; i++) {
            parent.addPartRaw(segments.get(i));
        }
        return parent;
    }

    public static FilePath from(FilePath filePath) {
        return new FilePath().addPartRaw(filePath.toString());
    }

    private FilePath inSegmentRange(int to) {
        FilePath filePath = new FilePath();


        for (int i = 0; i < to; i++) {
            filePath.addPartRaw(segments.get(i));
        }
        return filePath;
    }

    public String getSegment(int index) {
        return segments.get(index);
    }

    public String getFileName() {

        return segments.size() > 0 ? segments.get(segments.size() - 1) : "/";
    }

    public String getPath() {

//        return segments.get(segments.size() - 1);
        int size = segments.size();
        if (size == 0) {
            return "";
        } else if (size == 1) {
            return "/";
        }

        return inSegmentRange(size - 1).toString();
    }

    public int getSize() {
        return segments.size();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("/","/","");
        for (String segment : segments) {
            joiner.add(segment);
        }
        return joiner.toString();
    }
}
