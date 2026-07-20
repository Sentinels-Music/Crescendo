package Crescendo.controllers;

public class SearchResult {
    private String title;
    private String type; 
    private String additionalInfo; // release year, username etc based on type
    private double averageRating;
    
    public SearchResult(String title, String type, String additionalInfo, double averageRating) {
        this.title = title;
        this.type = type;
        this.additionalInfo = additionalInfo;
        this.averageRating = averageRating;
    }

    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getAdditionalInfo() { return additionalInfo; }
    public double getAverageRating() { return averageRating; }

    @Override
    public String toString() {
        return title + " (" + type + ") - " + additionalInfo;
    }
}