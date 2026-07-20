package model;

import java.util.Map;
import java.util.HashMap;

public class Song extends MusicItem {
    private int durationInSeconds;
    private Map<String, String> streamingLinks;

    public Song(int itemId, String title, int durationInSeconds) {
        super(itemId, title);
        this.durationInSeconds = durationInSeconds;
        this.streamingLinks = new HashMap<>();
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

    public Song getItemType() {
        return this;
    }
}
