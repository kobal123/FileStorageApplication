package com.kobal.FileStorageApp.fileservice;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    public FilePath addPartEncoded(String s) {
        if (s == null || s.length() == 0)
            return this;
        String[] parts = s.split("/");
        for (String part : parts) {
            segments.add(URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return this;
    }

    public FilePath addPartDecoded(String s) {
        if (s == null || s.length() == 0)
            return this;
        String[] parts = s.split("/");
        for (String part : parts) {
            segments.add(URLDecoder.decode(part.replace("%20", "+"), StandardCharsets.UTF_8));
        }
        return this;
    }

    public FilePath addPartRaw(String s) {
        if (s == null || s.length() == 0)
            return this;
        segments.add(s);
        return this;
    }

    public FilePath addPartRaw(FilePath filePath) {
        addPartRaw(filePath.toString());
        return this;
    }


    public FilePath addPartRawCopy(String s) {

        return new FilePath()
                .addPartRaw(this.toString())
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

    public static FilePath inSegmentRange(int from, int to, FilePath in) {
        FilePath filePath = new FilePath();
        int max = Math.min(in.getSize(), to);
        int min = Math.max(0, from);

        for (int i = min; i < max; i++) {
            filePath.addPartEncoded(in.getSegment(i));
        }
        return filePath;
    }

    public String getSegment(int index) {
        return segments.get(index);
    }

    public String getFileName() {
        return segments.get(segments.size() - 1);
    }

    public int getSize() {
        return segments.size();
    }

    @Override
    public String toString() {
        return String.join("/",segments);
    }
}
