package id.ac.ui.cs.mobileprogramming.izzanfi.musicx;

public class Song {

    private long id;
    private String title;
    private String artist;
    private String album;
    private String url;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, String songUrl) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        url = songUrl;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getUrl() {
        return url;
    }
}
