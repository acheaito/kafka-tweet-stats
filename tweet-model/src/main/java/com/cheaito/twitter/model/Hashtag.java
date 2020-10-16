package com.cheaito.twitter.model;

public class Hashtag {
    private final String tag;
    private final int start;
    private final int end;

    public Hashtag(String tag, int start, int end) {
        this.tag = tag;
        this.start = start;
        this.end = end;
    }

    /**
     * @deprecated Necessary for serialization
     */
    @Deprecated
    private Hashtag() {
        tag = null;
        start = 0;
        end = 0;
    }

    public String getTag() {
        return tag;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
