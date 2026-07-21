package com.crescendo.model;

/**
 * Stub owned by Mustafa Ziya Akyol (Music Items & Listen Later).
 * Implemented by MusicItem so any Album or Song can be bookmarked to a user's
 * "Listen Later" list the same way.
 */
public interface ListenLaterItem {
    void addToListenLater(User user);
    void removeFromListenLater(User user);
    boolean isInListenLater(User user);
}
