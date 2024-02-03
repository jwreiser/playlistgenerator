package com.goodforallcode.playlistgenerator.rest;

import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.model.domain.musicbrainz.MusicBrainzCallResults;
import com.goodforallcode.playlistgenerator.model.domain.spotify.SpotifyAlbumItem;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.MusicBrainzRestCaller;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SpotifyRestCallerTests {
    SpotifyRestCaller caller=new SpotifyRestCaller();
    @Test
    void testGetAccessToken() {
        String token=caller.getAccessToken();
        assertNotNull(token);
    }

    @Test
    void testGetAlbum() {
        String token=caller.getAccessToken();
        assertNotNull(token);
        SpotifyAlbumItem album = caller.getAlbum("Beastie Boys", "Ill Communication", token, false);
        assertNotNull(album);
        assertEquals("Beastie Boys",album.getArtists().get(0).getName());
        assertTrue(album.getName().toLowerCase().contains("ill communication"));
        album = caller.getAlbum("Beastie Boys", "Check Your Head", token, false);
        assertNotNull(album);
        assertEquals("Beastie Boys",album.getArtists().get(0).getName());
        assertTrue(album.getName().toLowerCase().contains("check your head"));

    }

    @Test
    void testGetPlaylistId() {
        String token=caller.getAccessToken();
        assertNotNull(token);
        String id = caller.getPlaylistId("savecuomo", "Comprehensive", token);
        assertNotNull(id);
        assertEquals("7owu50ooTT9XP6ywjkN1WT",id);
        id = caller.getPlaylistId("savecuomo", "Salsa Trap", token);
        assertNotNull(id);
        assertEquals("2Kcu5byMQm5qweZiFDoMe1",id);

    }

    //this only will work locally@Test
    void testAddPlaylistInformationToTracks() throws IOException {
        String token=caller.getAccessToken();
        assertNotNull(token);
        Path path= Path.of("C:\\Users\\justi\\OneDrive\\Desktop\\backup\\music\\Burned\\dance\\Various Artists\\fabriclive 33");
        List<Path> paths = Files.walk(path, 1).filter(Files::isRegularFile).collect(Collectors.toList());
        List<File> files=new ArrayList<>();
        for(Path currPath:paths){
            if(currPath.toAbsolutePath().toString().endsWith(".mp3")) {
                files.add(currPath.toFile());
            }
        }
        Album album=new Album("Various Artists", "True Blood Soundtrack",files);
        assertEquals(0,album.getTracks().size());//should be 0 prior to call
        assertTrue(caller.addPlaylistInformationToTracks("Various Artists", "fabriclive 33", token,false,album));
        assertEquals(files.size(),album.getTracks().size());//should be full after
    }

    //this only will work locally@Test
    void testPopulateAlbumWithTrackSearch() throws IOException {
        String token=caller.getAccessToken();
        assertNotNull(token);
        Path path= Path.of("C:\\Users\\justi\\OneDrive\\Desktop\\backup\\music\\Burned\\blues\\Various Artists\\The blues");
        List<Path> paths = Files.walk(path, 1).filter(Files::isRegularFile).collect(Collectors.toList());
        List<File> files=new ArrayList<>();
        for(Path currPath:paths){
            if(currPath.toAbsolutePath().toString().endsWith(".mp3")) {
                files.add(currPath.toFile());
            }
        }
        Album album=new Album("Various Artists", "Turntables on the Hudson, Vol. 3",files);
        assertEquals(0,album.getTracks().size());//should be 0 prior to call
        assertTrue(caller.populateAlbumWithTrackSearch(album, token));
        assertTrue(album.getTracks().size()>0);
    }
}
