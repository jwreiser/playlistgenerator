package com.goodforallcode.playlistgenerator.playlistgenerator.service;

import com.goodforallcode.playlistgenerator.javafx.PlaylistGeneratingTask;
import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.wrapper.spotify.SpotifyApi;
import javafx.scene.control.ProgressBar;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaylistGenerationService {
    static SongInformationService songService =new SongInformationService();
    static MusicBrainzAlbumService musicBrainzAlbumService =new MusicBrainzAlbumService();
    static SpotifyAlbumService spotifyAlbumService=new SpotifyAlbumService();

    public boolean addDirectoriesToPlayList(List<Path>pathList, String userName, String token, String playlistId, SpotifyApi spotifyApi,
                                            PlaylistGeneratingTask task, ProgressBar progressBar, boolean manuallyConfigureMp3Tags, boolean disambiguate) {
        boolean success=false;
        List<Album> workingAlbums = new ArrayList<>();
        for (
                Path path : pathList) {
            final String directoryPath = path.toAbsolutePath().toString();
            Set<Mp3Info> songs = new HashSet<>();
            songService.getSongInformation(songs, directoryPath, manuallyConfigureMp3Tags, null, progressBar);
            List<Album> albums = musicBrainzAlbumService.getAlbums(directoryPath, disambiguate);
            for (Album album : albums) {
                if (!Mp3FileUtil.wasEveryFilePublished(album)) {
                    workingAlbums.add(album);
                }
            }
        }
        if(!workingAlbums.isEmpty()) {
            try {
                spotifyAlbumService.addAlbumsToPlaylist(userName, token, playlistId, workingAlbums, spotifyApi, manuallyConfigureMp3Tags, disambiguate);
                success = true;
            } catch (
                    URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        System.err.println("Finished with "+pathList);
        return success;
    }
}
