package com.goodforallcode.playlistgenerator.playlistgenerator.model;

import com.mpatric.mp3agic.ID3v1Genres;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Mp3Info {
    String title;
    String artist;
    String album;
    int genre;
    String genreDescription;
    boolean containsTrackInfo=false;
    public Mp3Info(String title, String artist, String album) {
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public Mp3Info(String title, String artist, String album, int genre,String genreDescription) {
        this(title,artist,album);
        this.genre=genre;
        this.genreDescription=genreDescription;
    }

    public Mp3Info(String title,boolean containsTrackInfo) {
        this.title = title;
        this.containsTrackInfo = containsTrackInfo;
    }
    @Override
    public String toString() {
        return title+" - "+artist+" - "+album;
    }

}
