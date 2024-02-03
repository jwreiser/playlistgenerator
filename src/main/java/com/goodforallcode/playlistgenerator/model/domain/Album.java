package com.goodforallcode.playlistgenerator.model.domain;

import com.goodforallcode.playlistgenerator.model.domain.spotify.SpotifyAlbumItem;
import com.goodforallcode.playlistgenerator.model.domain.spotify.SpotifyTrack;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data

public class Album {
    String artist;
    String  name;
    List<File>files;
    SpotifyAlbumItem albumItem;
    List<SpotifyTrack> tracks=new ArrayList<>();
    public Album(String artist, String name, List<File> files) {
        this.artist = artist;
        this.name = name;
        this.files = files;
    }
}
