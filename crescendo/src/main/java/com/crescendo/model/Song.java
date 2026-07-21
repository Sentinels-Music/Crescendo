package com.crescendo.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stub owned by Mustafa Ziya Akyol (Music Items & Listen Later).
 * Streaming links are kept as a Map<String,String> (platform -> url) per the UML class
 * diagram, rather than a separate StreamingLink class.
 */
public class Song extends MusicItem implements ListenLaterItem {

    private final int durationInSeconds;
    private final Map<String, String> streamingLinks = new LinkedHashMap<>();

    public Song(int itemId, String title, Artist artist, int durationInSeconds) {
        super(itemId, title, artist);
        this.durationInSeconds = durationInSeconds;
    }

    public void addStreamingLink(String platform, String url) {
        streamingLinks.put(platform, url);
    }

    public void removeStreamingLink(String platform) {
        streamingLinks.remove(platform);
    }

    public String getStreamingLink(String platform) {
        return streamingLinks.get(platform);
    }

    public Map<String, String> getStreamingLinks() {
        return streamingLinks;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    @Override
    public String getItemType() {
        return "Song";
    }

    @Override
    public void addToListenLater(User user) {
        user.addToListenLater(this);
    }

    @Override
    public void removeFromListenLater(User user) {
        user.removeFromListenLater(this);
    }

    @Override
    public boolean isInListenLater(User user) {
        return user.getListenLaterItems().contains(this);
    }
}
