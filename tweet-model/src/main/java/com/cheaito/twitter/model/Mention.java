package com.cheaito.twitter.model;

public class Mention {
    private final String username;
    private final int start;
    private final int end;

    public Mention(String username, int start, int end) {
        this.username = username;
        this.start = start;
        this.end = end;
    }

    @Deprecated
    private Mention() {
        username = null;
        start = 0;
        end = 0;
    }

    public String getUsername() {
        return username;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
