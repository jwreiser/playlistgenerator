package com.goodforallcode.playlistgenerator.playlistgenerator.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.model.domain.spotify.*;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.StringUtil;
import com.mpatric.mp3agic.*;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.JSONObject;
import com.goodforallcode.playlistgenerator.model.domain.musicbrainz.MusicBrainzCallResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SpotifyRestCaller {
    private static LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
    private static String clientId = "9b2d07022df44017b911b6176dc19766";
    private static String secret = "9154ba61d07940ee8f03c50f98ff88fb";
    private static int ALBUM_NAME_DISTANCE_CUTOFF = 5;
    private static int ARTIST_NAME_DISTANCE_CUTOFF = 5;
    private static int TRACK_NAME_DISTANCE_CUTOFF = 10;
    private static int DURATION_CUTOFF = 20_000;

    public String getAccessToken() {
        MusicBrainzCallResults musicBrainzCallResults = null;
        String token = null;
        try {
            String urlString = "https://accounts.spotify.com/api/token?grant_type=client_credentials&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString())
                    + "&client_secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8.toString());

            String json = callUrl("POST", urlString, null);
            if (json != null) {
                JSONObject object = new JSONObject(json);
                if (object.has("access_token")) {
                    token = String.valueOf(object.get("access_token"));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return token;
    }

    public void populateAdditionalSpotifyInformation(List<Album> albums, String token) {
        List<String> albumIds = albums.stream().filter(a -> a.getAlbumItem() != null).map(a -> a.getAlbumItem().getId()).collect(Collectors.toList());
        String joinedIds = String.join(",", albumIds);
        try {
            String urlString = "https://api.spotify.com/v1/albums?ids=" + URLEncoder.encode(joinedIds, StandardCharsets.UTF_8.toString());

            String json = callUrl("GET", urlString, token);
            ObjectMapper objectMapper = new ObjectMapper();
            SpotifyAlbumListResults callResults = objectMapper.readValue(json, SpotifyAlbumListResults.class);
            for (SpotifyAlbumItem resultItem : callResults.getAlbums()) {
                for (Album album : albums) {
                    if (album.getAlbumItem() != null && album.getAlbumItem().getId().equals(resultItem.getId())) {
                        album.setAlbumItem(resultItem);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getPlaylistId(String userName, String playlistName, String token) {
        String result = null;
        try {
            String urlString = "https://api.spotify.com/v1/users/" + URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
                    + "/playlists?limit=50";

            String json = callUrl("GET", urlString, token);
            ObjectMapper objectMapper = new ObjectMapper();
            SpotifyPlaylistResults callResults = objectMapper.readValue(json, SpotifyPlaylistResults.class);
            for (SpotifyPlaylist playlist : callResults.getPlaylists()) {
                if (playlist.getOwner().getId().equals(userName) && playlist.getName().equals(playlistName)) {
                    result = playlist.getId();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public SpotifyAlbumItem getAlbum(String artist, String album, String token, boolean compilation) {
        SpotifyAlbumItem results = null;
        album = album.toLowerCase();
        try {
            String urlString = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode((album + " " + artist), StandardCharsets.UTF_8.toString())
                    + "&type=album";

            String json = callUrl("GET", urlString, token);
            if (json != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                SpotifyAlbumResults callResults = objectMapper.readValue(json, SpotifyAlbumResults.class);

                int closestMatch = Integer.MAX_VALUE, currentDistance;
                String spotifyAlbumName;
                for (SpotifyAlbumItem albumItem : callResults.getAlbumsContainer().getAlbums()) {
                    spotifyAlbumName = albumItem.getName().toLowerCase();
                    if (isCorrectAlbumType(albumItem.getName(), albumItem.getAlbumType(), compilation)) {
                        if (compilation || albumHasCorrectArtist(artist, albumItem)) {
                            if (spotifyAlbumName.equals(album)) {
                                results = albumItem;
                                break;
                            } else {
                                currentDistance = distanceCalculator.apply(album, spotifyAlbumName);
                                if (currentDistance < closestMatch) {
                                    closestMatch = currentDistance;
                                    if (currentDistance < ALBUM_NAME_DISTANCE_CUTOFF) {
                                        results = albumItem;
                                    } else if (album.length() > 5 && spotifyAlbumName.length() > 5 && (album.contains(spotifyAlbumName) || spotifyAlbumName.contains(album))) {
                                        results = albumItem;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return results;
    }

    public boolean populateAlbumWithTrackSearch(Album album, String token) {
        boolean results = true;
        boolean currentResults;
        Mp3File mp3;
        for (File file : album.getFiles()) {
            if (!Mp3FileUtil.fileHasSpotifyInformation(file)) {
                try {
                    mp3 = new Mp3File(file);
                    ID3v1 tag = Mp3FileUtil.getTag(mp3);
                    if (tag.getArtist()==null || tag.getArtist().toLowerCase().equals("various artists")) {
                        currentResults = populateFileWithTrackSearchVariousArtists(tag.getTitle(), token, file.toPath(), album, mp3, tag);
                    } else {
                        currentResults = populateFileWithTrackSearch(tag.getArtist(), tag.getTitle(), token, file.toPath(), album, mp3, tag);
                    }
                    if (!currentResults) {
                        results = false;//keep on going though
                    }
                } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                    //skip
                }

            }
        }
        return results;
    }

    private boolean populateFileWithTrackSearchVariousArtists(String trackName, String token, Path file, Album album, Mp3File mp3, ID3v1 tag) {
        boolean results = false;
        trackName = StringUtil.removeTrailingText(trackName.toLowerCase());
        long duration = mp3.getLengthInMilliseconds();
        try {
            String urlString = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode((trackName), StandardCharsets.UTF_8.toString())
                    + "&type=track&limit=50";

            String json = callUrl("GET", urlString, token);
            long bestDurationDifference = Integer.MAX_VALUE, currentDurationDifference;
            SpotifyTrack bestTrack = null;
            if (json != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                SpotifyTrackSearchResults callResults = objectMapper.readValue(json, SpotifyTrackSearchResults.class);
                for (SpotifyTrack track : callResults.getTracksContainer().getTracks()) {
                    if (track.getName().toLowerCase().contains(trackName)) {
                        currentDurationDifference = Math.abs(track.getDuration() - duration);
                        if (currentDurationDifference < DURATION_CUTOFF && currentDurationDifference < bestDurationDifference) {
                            bestDurationDifference = currentDurationDifference;
                            bestTrack = track;
                            results = true;
                        }
                    }
                }
                if (bestTrack != null) {
                    tag.setArtist(bestTrack.getArtists().get(0).getName());
                    tag.setComment("spotifyTrackId:" + bestTrack.getId() + "; spotifyAlbumId:" + bestTrack.getAlbum().getId());
                    album.getTracks().add(bestTrack);
                    results = Mp3FileUtil.saveMp3(file, mp3);
                } else {
                    tag.setComment("spotifyExistenceCheckFailed:" + LocalDate.now().toString());
                    Mp3FileUtil.saveMp3(file, mp3);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return results;
    }

    private boolean populateFileWithTrackSearch(String artist, String trackName, String token, Path file, Album album, Mp3File mp3, ID3v1 tag) {
        boolean results = false;

        String originalTrackName=trackName;
        long duration=mp3.getLengthInMilliseconds();
        String[] artists = artist.toLowerCase().replaceFirst("dj ", "").split("/");
        try {
            String urlString = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode((trackName + " " + artist), StandardCharsets.UTF_8.toString())
                    + "&type=track&limit=50";

            //do after the search as it removes spaces
            trackName = StringUtil.cleanupTrack(trackName);

            String json = callUrl("GET", urlString, token);
            long bestDurationDifference = Integer.MAX_VALUE, currentDurationDifference;
            int currentDistance;
            SpotifyTrack bestTrack = null;
            String modifiedTrackName,modifiedTrackArtist,modifiedTargetArtist;
            if (json != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                SpotifyTrackSearchResults callResults = objectMapper.readValue(json, SpotifyTrackSearchResults.class);

                for (SpotifyTrack track : callResults.getTracksContainer().getTracks()) {
                    if (results) {
                        break;
                    }
                    modifiedTrackName=StringUtil.cleanupTrack(track.getName());
                    for (SpotifyArtist trackArtist : track.getArtists()) {
                        //intentionally making this more similar by removing whitespace as that could make them different
                        modifiedTrackArtist=StringUtil.cleanupArtist(trackArtist.getName());

                        if (trackName.equals(modifiedTrackName)) {
                            for (String artistName : artists) {
                                modifiedTargetArtist=StringUtil.cleanupArtist(artistName);
                                if (distanceCalculator.apply(modifiedTrackArtist,modifiedTargetArtist)<ARTIST_NAME_DISTANCE_CUTOFF) {
                                    tag.setComment("spotifyTrackId:" + track.getId() + "; spotifyAlbumId:" + track.getAlbum().getId());
                                    album.getTracks().add(track);
                                    results = Mp3FileUtil.saveMp3(file, mp3);
                                    break;
                                }
                            }
                            break;
                        } else {
                            currentDistance = distanceCalculator.apply(modifiedTrackName, trackName);
                            currentDurationDifference= Math.abs(track.getDuration()-duration);
                            if (currentDurationDifference < bestDurationDifference) {
                                if (currentDistance < TRACK_NAME_DISTANCE_CUTOFF || trackName.contains(modifiedTrackName)
                                        || modifiedTrackName.contains(trackName)) {
                                    for (String artistName : artists) {
                                        modifiedTargetArtist=StringUtil.cleanupArtist(artistName);
                                        if (modifiedTrackArtist.contains(modifiedTargetArtist)||modifiedTargetArtist.contains(modifiedTrackArtist)) {
                                            bestDurationDifference = currentDistance;
                                            bestTrack = track;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!results) {
                    if (bestTrack == null) {
                        if (StringUtil.hasTrailingText(trackName)||artist.toLowerCase().contains("feat")||artist.toLowerCase().contains("ft.")) {
                            int end=artist.toLowerCase().indexOf("feat");
                            if(end>0){
                                artist=artist.substring(0,end).trim();
                            }else{
                                end=artist.toLowerCase().indexOf("ft.");
                                if(end>0){
                                    artist=artist.substring(0,end).trim();
                                }
                            }
                            String originalTrackNameWithoutTrailing=StringUtil.removeTrailingText(originalTrackName);
                            results = populateFileWithTrackSearch(artist,originalTrackNameWithoutTrailing , token, file, album, mp3, tag);
                        } else {
                            tag.setComment("spotifyExistenceCheckFailed:" + LocalDate.now().toString());
                            Mp3FileUtil.saveMp3(file, mp3);
                        }
                    } else {
                        tag.setComment("spotifyTrackId:" + bestTrack.getId() + "; spotifyAlbumId:" + bestTrack.getAlbum().getId());
                        album.getTracks().add(bestTrack);
                        results = true;
                        Mp3FileUtil.saveMp3(file, mp3);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return results;
    }


    public boolean addPlaylistInformationToTracks(String artist, String albumName, String token, boolean compilation, Album album) {
        boolean results = false;
        albumName = StringUtil.cleanupAlbum(albumName);

        boolean variousArtists=false;
        if(List.of("various","various artists").contains(artist.toLowerCase())){
            variousArtists=true;
        }

        artist = StringUtil.cleanupArtist(artist);

        try {
            String urlString = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode((albumName + " " + artist), StandardCharsets.UTF_8.toString())
                    + "&type=playlist";
            if (compilation||variousArtists) {
                urlString = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode((albumName), StandardCharsets.UTF_8.toString())
                        + "&type=playlist";
            }
            String json = callUrl("GET", urlString, token);
            List<String> matchedTracks = new ArrayList<>();
            List<SpotifyTrackContainer> trackContainers;
            Mp3File mp3;
            String trackTitle,modifiedPlaylistName;
            ID3v1 tag = null;
            int totalTracks = album.getFiles().size();
            if (json != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                SpotifySearchPlaylistResults callResults = objectMapper.readValue(json, SpotifySearchPlaylistResults.class);
                for (SpotifyPlaylist playlist : callResults.getPlaylistContainer().getPlaylists()) {
                    modifiedPlaylistName=StringUtil.cleanupAlbum(playlist.getName());
                    if (modifiedPlaylistName.contains(albumName) && (variousArtists||modifiedPlaylistName.contains(artist))) {
                        if (results) {
                            break;
                        }

                        trackContainers = getPlaylistTracks(playlist.getTrackInformation().getHref(), token);
                        for (File file : album.getFiles()) {
                            try {
                                mp3 = new Mp3File(file);
                                tag = Mp3FileUtil.getTag(mp3);

                                if (tag != null) {
                                    trackTitle = StringUtil.cleanupTrack(tag.getTitle());
                                    if (matchedTracks.contains(trackTitle)) {
                                        continue;
                                    } else {
                                        if (addTrackInformationToAlbumAndFile(trackContainers, file, mp3, album, tag, trackTitle)) {
                                            matchedTracks.add(trackTitle);
                                            if (matchedTracks.size() == totalTracks) {
                                                results = true;
                                                break;//we have matched them all, leave this playlists loop
                                            }
                                        }
                                    }
                                }
                            } catch (UnsupportedTagException | InvalidDataException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return results;
    }


    private boolean addTrackInformationToAlbumAndFile(List<SpotifyTrackContainer> trackContainers, File file, Mp3File mp3, Album album, ID3v1 tag, String trackTitle) {
        boolean success = false;
        SpotifyTrack track;
        String trackName;
        for (SpotifyTrackContainer container : trackContainers) {
            track = container.getTrack();
            trackName=StringUtil.cleanupTrack(track.getName());
            if (trackName.contains(trackTitle)) {
                if (album != null) {
                    album.getTracks().add(track);
                }
                tag.setComment("spotifyTrackId:" + track.getId() + "; spotifyAlbumId:" + track.getAlbum().getId());
                success = Mp3FileUtil.saveMp3(file.toPath(), mp3);
                break;
            }
        }
        return success;
    }

    private List<SpotifyTrackContainer> getPlaylistTracks(String urlString, String token) {
        List<SpotifyTrackContainer> results = null;

        try {

            String json = callUrl("GET", urlString, token);
            if (json != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                SpotifyPlaylistTracksResults callResults = objectMapper.readValue(json, SpotifyPlaylistTracksResults.class);
                results = callResults.getTrackContainers();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return results;
    }

    private static boolean albumHasCorrectArtist(String artist, SpotifyAlbumItem albumItem) {
        String modifiedArtist = artist.toLowerCase();
        return albumItem.getArtists().stream().anyMatch(a -> modifiedArtist.equals(a.getName().toLowerCase()));
    }

    private boolean isCorrectAlbumType(String albumName, String albumType, boolean isCompilation) {
        boolean isCorrectAlbumType = false;
        if (isCompilation) {
            if (albumType.equals("compilation")) {
                isCorrectAlbumType = true;
            }
        } else if (albumType.equals("album")) {
            isCorrectAlbumType = true;
        } else {
            List<String> compilationKeywords = List.of(" hits", "best ", "worst ", "ultimate ", "greatest ");
            String modifiedAlbumName = albumName.toLowerCase();
            if (compilationKeywords.stream().anyMatch(k -> modifiedAlbumName.contains(k)) && albumType.equals("compilation")) {
                isCorrectAlbumType = true;
            }
        }
        return isCorrectAlbumType;
    }

    private static String callUrl(String method, String urlString, String token) throws IOException {
        URL url = new URL(urlString);
        String json = null;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod(method);
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        } else {
            connection.setDoOutput(true);
            connection.getOutputStream().close();
        }


        // Collect the response code
        int responseCode = connection.getResponseCode();
        System.out.println(method + " Response Code :: " + responseCode);

        if (responseCode == connection.HTTP_OK) {
            // Create a reader with the input stream reader.
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));) {

                String inputLine;

                // Create a string buffer
                StringBuffer response = new StringBuffer();

                // Write each of the input line
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                json = response.toString();
                //                ObjectMapper objectMapper = new ObjectMapper();
                //                musicBrainzCallResults = objectMapper.readValue(json, MusicBrainzCallResults.class);
            }
        }
        return json;
    }


}
