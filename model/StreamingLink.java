package model;

import java.awt.Desktop;
import java.net.URI;


public class StreamingLink {

    private int linkId; 
    private int itemId;    
    private String platform;  
    private String url;        
    public StreamingLink(int itemId, String platform, String url) {
        this(0, itemId, platform, url);
    }

    public StreamingLink(int linkId, int itemId, String platform, String url) {
        this.linkId = linkId;
        this.itemId = itemId;
        this.platform = platform;
        this.url = url;
    }

    // Returns the streaming URL of this link. 
    public String getStreamingLink() {
        return url;
    }

    // Opens the streaming link in the user's default web browser
    public void open() {
        try {
            if (url == null || url.isEmpty()) {
                System.out.println("No streaming link available.");
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback if the desktop cannot open a browser 
                System.out.println("Opening is not supported here. Link: " + url);
            }
        } catch (Exception e) {
            System.out.println("Could not open the link: " + e.getMessage());
        }
    }

    // getters / setters 
    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return platform + " -> " + url;
    }
}
