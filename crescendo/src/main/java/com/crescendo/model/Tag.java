package com.crescendo.model;

import java.util.Objects;

/**
 * Owned by Emir Selim Kayhan (Artist, Tag & Streaming).
 * Represents a music genre or style associated with artists and user profiles.
 */
public class Tag {

    private final int tagId;
    private final String name;

    public Tag(int tagId, String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public int getTagId() {
        return tagId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag tag)) return false;
        return tagId == tag.tagId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId);
    }

    @Override
    public String toString() {
        return name;
    }
}
