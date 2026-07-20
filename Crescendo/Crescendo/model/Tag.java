package Crescendo.model;

/**
 * A music genre or style (e.g. "Psychedelic", "Trip-hop"), backed by the Tags
 * table. Tags are attached to Artists (ArtistTags) and to user Profiles
 * (ProfileTags) so users can be matched by taste and discover music by genre.
 *
 * Author: Emir Selim Kayhan
 */
public class Tag {

    private int tagId;     // 0 until it has been saved to / loaded from the database
    private String name;

    public Tag(String name) {
        this(0, name);
    }

    public Tag(int tagId, String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Two tags count as the same when they have the same name (ignoring case).
    // This makes removeTag() and contains() work the way we expect, even
    // before a tag has been assigned a real tagId by the database.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tag)) {
            return false;
        }
        Tag other = (Tag) o;
        return name != null && name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
