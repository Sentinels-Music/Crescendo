package com.crescendo.panels;

import com.crescendo.model.Artist;
import com.crescendo.model.User;
import com.crescendo.model.VerifiedUser;
import javafx.stage.Stage;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The navigation callbacks every logged-in screen shares, bundled so screen build()
 * methods don't need a dozen individual Runnable/Consumer parameters.
 */
public final class Nav {
    public final Stage stage;
    public final User currentUser;
    public final boolean verified;
    public final Runnable toHome;
    public final Runnable toDiscover;
    public final Runnable toListenLater;
    public final Runnable toTasteMatch;
    public final Runnable toProfile;
    public final Consumer<String> toSearch;
    public final Consumer<Artist> toArtist;
    public final IntConsumer toSong;
    public final IntConsumer toAlbum;
    public final boolean canGoBack;
    public final Runnable toBack;
    public final Runnable toLogout;

    public Nav(Stage stage, User currentUser, boolean canGoBack, Runnable toHome, Runnable toDiscover,
               Runnable toListenLater, Runnable toTasteMatch, Runnable toProfile, Consumer<String> toSearch,
               Consumer<Artist> toArtist, IntConsumer toSong, IntConsumer toAlbum, Runnable toBack,
               Runnable toLogout) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.verified = currentUser instanceof VerifiedUser;
        this.toHome = toHome;
        this.toDiscover = toDiscover;
        this.toListenLater = toListenLater;
        this.toTasteMatch = toTasteMatch;
        this.toProfile = toProfile;
        this.toSearch = toSearch;
        this.toArtist = toArtist;
        this.toSong = toSong;
        this.toAlbum = toAlbum;
        this.canGoBack = canGoBack;
        this.toBack = toBack;
        this.toLogout = toLogout;
    }
}
