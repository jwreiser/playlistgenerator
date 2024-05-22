package com.goodforallcode.playlistgenerator.javafx;

import com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.PlaylistGenerationService;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller.clientId;
import static com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller.secret;

@Setter
public class PlaylistFileGeneratingTask extends Task<Void> implements Runnable{
    private static SpotifyRestCaller spotifyCaller = new SpotifyRestCaller();
    String directory;
    boolean manuallyConfigureMp3Tags;
    boolean disambiguate;
    ProgressBar progressBar;
    String userName;
    String playlistName;
    public PlaylistFileGeneratingTask(String directory, String userName, String playlistName, boolean manuallyConfigureMp3Tags, boolean disambiguate) {
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
        System.err.println("Getting path lists");
        List<Path> directories = Files.walk(Paths.get(directory), 10)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());
        System.err.println("Got path lists");
        List<String> pathStrings=directories.stream().map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toList());
        String filePath="C:\\temp\\playlist.txt";
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
                Paths.get(filePath)))){
            pathStrings.stream().forEach(pw::println);
        }


        return null;

    }



}
