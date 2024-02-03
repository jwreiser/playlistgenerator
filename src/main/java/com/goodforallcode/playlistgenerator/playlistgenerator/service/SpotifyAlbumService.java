package com.goodforallcode.playlistgenerator.playlistgenerator.service;

import com.goodforallcode.playlistgenerator.javafx.TooManyOptions;
import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.model.domain.spotify.SpotifyAlbumItem;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.google.common.collect.Lists;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SpotifyAlbumService {

    private static SpotifyRestCaller spotifyCaller = new SpotifyRestCaller();

    public void addAlbumsToPlaylist(String username, String playlistName, List<Album> allAlbums) {
        if (!allAlbums.isEmpty()) {
            String token = spotifyCaller.getAccessToken();
            if (token == null) {
                return;
            }

            String playlistId = spotifyCaller.getPlaylistId(username, playlistName, token);
            if (playlistId == null) {
                return;
            }
            SpotifyAlbumItem currentAlbumItem;
            boolean isCompilation;

            List<Album> albumsToUseInCall = new ArrayList<>();
            for (Album currentAlbum : allAlbums) {
                if (currentAlbum.getFiles().size() < 4) {
                    isCompilation = true;
                } else {
                    isCompilation = false;
                }

                currentAlbumItem = spotifyCaller.getAlbum(currentAlbum.getArtist(), currentAlbum.getName(), token, isCompilation);
                if (currentAlbumItem != null) {
                    currentAlbum.setAlbumItem(currentAlbumItem);
                    albumsToUseInCall.add(currentAlbum);
                } else if (!spotifyCaller.addPlaylistInformationToTracks(currentAlbum.getArtist(), currentAlbum.getName(), token, isCompilation,currentAlbum)) {
                    for(File file:currentAlbum.getFiles()){
                        try {
                            Mp3File mp3=new Mp3File(file);
                        } catch (IOException |UnsupportedTagException|InvalidDataException e) {
                            //skip the file
                        }
                    }
                }
            }
            Collection<List<Album>> lists = Lists.partition(allAlbums, 20);
            for(List<Album>list:lists) {
                spotifyCaller.populateAdditionalSpotifyInformation(list, token);
            }
        }

    }



}
