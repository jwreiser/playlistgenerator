package com.goodforallcode.playlistgenerator.javafx;

import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.SpotifyRestCaller;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.MusicBrainzAlbumService;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.SongInformationService;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.SpotifyAlbumService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import lombok.Setter;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Setter
public class PlaylistGeneratingTask extends Task<Void> implements Runnable{
    static SongInformationService songService =new SongInformationService();
    static MusicBrainzAlbumService musicBrainzAlbumService =new MusicBrainzAlbumService();
    static SpotifyAlbumService spotifyAlbumService=new SpotifyAlbumService();
    String directory;
    boolean manuallyConfigureMp3Tags;
    ProgressBar progressBar;
    String userName;
    String playlistName;
    public PlaylistGeneratingTask(String directory, String userName,String playlistName,boolean manuallyConfigureMp3Tags) {
        this.directory = directory;
        this.manuallyConfigureMp3Tags = manuallyConfigureMp3Tags;
        this.userName=userName;
        this.playlistName=playlistName;
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



        List<Path> directories = Files.walk(Paths.get(directory), 10)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());
        List<Path> leafDirectories=new ArrayList<>();
        boolean leafDirectory;
        for (int i=0;i<directories.size();i++) {
            final Path currentDirectory = directories.get(i);
            final String directoryPath=currentDirectory.toAbsolutePath().toString();
            leafDirectory = !Files.walk(currentDirectory, 1).anyMatch(f->isDirectory(f,directoryPath));
            if (leafDirectory) {
                leafDirectories.add(currentDirectory);
            }
        }
        Collection<List<Path>> pathLists = breakListIntoThreadSizeChunks(leafDirectories, 20);

        boolean first=true;
        for (List<Path> pathList:pathLists) {
            if(!first){
                break;
            }
            first=false;
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    List<Album> workingAlbums=new ArrayList<>();
                    for(Path path:pathList) {
                        final String directoryPath=path.toAbsolutePath().toString();
                        Set<Mp3Info> songs = new HashSet<>();
                        songService.getSongInformation(songs, directoryPath, manuallyConfigureMp3Tags, null, progressBar);
                        List<Album> albums = musicBrainzAlbumService.getAlbums(directoryPath);
                        for(Album album:albums){
//                            if(!spotifyAlbumService.doesEveryFileHaveSpotifyInformation(album)){
                                workingAlbums.add(album);
//                            }
                        }
                    }
                    spotifyAlbumService.addAlbumsToPlaylist(userName,playlistName,workingAlbums);

                }
            });
        }
//        songs = getSongInformation(directory,manuallyConfigureMp3Tags,this);

        return null;

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
