package model;

public interface ListenLaterItem {
    void addToListenLater(User user);
    void removeFromListenLater(User user);
    boolean isInListenLater(User user);
}
