package Crescendo.controllers;

import java.util.List;
public class CrescendoGetterSetters {

    /**
     * search in database
     * 
     * @param target String that wants to searched for.
     * @param filterType Category of the target ("All", "Artists", "Albums", "Songs", "People")
     * @return list of SearchResult objects to add UI
     * 
     *           -----SearchResult-----
     *  SearchResult(String title, String type, String additionalInfo, double averageRating)
     *  additionalInfo: has release year or username depending on the type.
     *  SearchResult have these methods:
     *    - void getTitle()
     *    - void getType()
     *    - void getAdditionalInfo()
     *    - void getAverageRating()
     */
    public static List<SearchResult> executeSearch(String target, String filterType) {
        SearchController searchController = new SearchController();
        return searchController.searchMusicItem(target, filterType);
    }

    /**
     * calculates the taste match percentage
     *  Taste Match Percentage = (Count of Shared Tags) / (Total Tags of Current Users) * 100 rounded to nearest number
     *                           
     * 
     * @param currentUserId the id of the logged-in user
     * @param targetUserId the id of the target user(followed user)
     * @return percentage of taste match
     */
    public static int getTasteMatch(int currentUserId, int targetUserId) {
        TasteMatchController tasteController = new TasteMatchController();
        return tasteController.getTasteMatchPercentage(currentUserId, targetUserId);
    }

    /**
     * 
     * @param userId Id of user who wants to adds reviews.
     * @param itemId Id of the song/album
     * @param stars Amount of stars that is going to added.
     * @param comment The comment
     * @return true if succesfull, false if it encounters an error
     */
    public static boolean addReview(int userId, int itemId, int stars, String comment) {
        return new ReviewController().insertReview(userId, itemId, stars, comment);
    }
}