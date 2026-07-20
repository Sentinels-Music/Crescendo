package model;

import java.util.List;
import java.util.ArrayList;

public class Song extends MusicItem {
    private int durationInSeconds;
    private List<StreamingLink> streamingLinks;

    public Song(int itemId, String title, int durationInSeconds, double averageRating) {
        super(itemId, title, averageRating);
        this.durationInSeconds = durationInSeconds;
        this.streamingLinks = new ArrayList<>();
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public String getFormattedDuration() {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    public void addStreamingLink(StreamingLink link) {
        streamingLinks.add(link);
    }

    public StreamingLink getStreamingLink() {
        if (streamingLinks.isEmpty()) {
            return null;
        }
        return streamingLinks.get(0);
    }

    public List<StreamingLink> getStreamingLinks() {
        return streamingLinks;
    }

    public Song getItemType() {
        return this;
    }
}
