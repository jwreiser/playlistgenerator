package com.goodforallcode.playlistgenerator.javafx;

import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.MusicBrainzAlbumService;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.PlaylistGenerationService;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.SongInformationService;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.SpotifyAlbumService;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller.clientId;
import static com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller.secret;

@Setter
public class PlaylistGeneratingTask extends Task<Void> implements Runnable{
    private static SpotifyRestCaller spotifyCaller = new SpotifyRestCaller();
    String directory;
    boolean manuallyConfigureMp3Tags;
    boolean disambiguate;
    ProgressBar progressBar;
    String userName;
    String playlistName;
    public PlaylistGeneratingTask(String directory, String userName,String playlistName,boolean manuallyConfigureMp3Tags,boolean disambiguate) {
        this.directory = directory;
        this.manuallyConfigureMp3Tags = manuallyConfigureMp3Tags;
        this.userName=userName;
        this.playlistName=playlistName;
        this.disambiguate=disambiguate;
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected Void call() throws Exception {
        //AUth
        //Step 1 get code
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(secret)
                .setRedirectUri(new URI("http://localhost:3000"))
                .build();



        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri().scope("playlist-modify-public").build();
        URI execute = authorizationCodeUriRequest.execute();
        String code="AQCb3bkE4KUNVuS_FwNOfDbmTJVcj-j7CqaCs8cPhnjvOR6rCFxMQqxkDIi0rVt_9pNCJToZFJg86COB5KM7ZkHZVarsgpfLmifROLcZqdmNmULnOd5uwg1SZBBvcnp2DCL-UVMRtPWAvVl4kA9BS-o1v-uH52Zpt0q1SiyCj0Bgn5ZL-W9b8zB7Vz_DxvIHkw";

        System.err.println("Getting tokens");

        //step 2 get tokens
        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
        AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

        // Set access and refresh token for further "spotifyApi" object usage
        spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
        spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

        String token = spotifyCaller.getAccessToken();
        String playlistId = spotifyCaller.getPlaylistId(userName, playlistName, token);
        if (token == null||playlistId == null) {
            return null;
        }
        System.err.println("Getting path lists");
        Collection<List<Path>> pathLists = getPathLists();
        System.err.println("Got path lists. Size:"+pathLists.size());
        for (List<Path> pathList:pathLists) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    PlaylistGenerationService service=new PlaylistGenerationService();
                    service.addDirectoriesToPlayList(pathList, userName, token,playlistId,  spotifyApi,
                            null, progressBar,  manuallyConfigureMp3Tags,  disambiguate);
                }
            });
        }

        return null;

    }

    @NotNull
    private Collection<List<Path>> getPathLists() throws IOException {
        List<Path> directories = Files.walk(Paths.get(directory), 10)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());
        List<Path> leafDirectories=new ArrayList<>();
        boolean leafDirectory;
        for (int i=0;i<directories.size();i++) {
            final Path currentDirectory = directories.get(i);
            final String directoryPath=currentDirectory.toAbsolutePath().toString();
            leafDirectory = !Files.walk(currentDirectory, 1).anyMatch(f->isDirectory(f,directoryPath));
            if (leafDirectory && !Mp3FileUtil.wasEveryFilePublished(currentDirectory)) {
                leafDirectories.add(currentDirectory);
            }
        }
        Collection<List<Path>> pathLists = breakListIntoThreadSizeChunks(leafDirectories, 12);
        return pathLists;
    }

    private static Collection<List<Path>> breakListIntoThreadSizeChunks(List<Path> list, int numThreads) {
        List<List<Path>> results=new ArrayList<List<Path>>();
        for(int i=0;i<numThreads;i++){
            results.add(new ArrayList<>());
        }
        List<Path> currentList;
        int currentListIndex;
        for(int i=0;i<list.size();i++){
            currentListIndex=i%numThreads;
            currentList=results.get(currentListIndex);
            currentList.add(list.get(i));
        }
        /*int desiredListSize = 1;
        if (list.size() > numThreads) {
            desiredListSize = list.size() / numThreads;
        }
        did not disperse evenly Collection<List<Path>> lists = Lists.partition(list, desiredListSize);

         */

        return results;
    }
    private boolean isDirectory(Path file,String directoryPath){
        if(!file.toFile().isDirectory()){
            return false;
        }else if (file.toAbsolutePath().toString().equals(directoryPath)){
            return false;//should not count the current directory
        }else {
            return true;
        }
    }
}
