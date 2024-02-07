package com.goodforallcode.playlistgenerator.playlistgenerator.service;

import com.goodforallcode.playlistgenerator.javafx.TooManyOptions;
import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MusicBrainzAlbumService {



    static synchronized String useChooser(Path directory, String optionName, List<String> options){
        String name=Thread.currentThread().getName();
        System.err.println("thread "+name+" in");
        TooManyOptions chooser=new TooManyOptions(directory, optionName, options);
        chooser.show();
        System.err.println("thread "+name+" out");
        return chooser.getChoice();
    }
    public List<Album> getAlbums(String currentDirectory,boolean disambiguate) {
        List<Album> albums = new ArrayList<>();
        try {
            List<Path> dirs = Files.walk(Paths.get(currentDirectory), 10)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());

            for (Path directory : dirs) {
                albums.addAll(getAlbumsFromDirectory(directory,disambiguate));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return albums;
    }

    private List<Album> getAlbumsWhenOneArtist(Path directory, HashMap<String, HashMap<String, List<File>>> trackInformation, int numFiles,boolean disambiguate) {
        List<Album> albums = new ArrayList<>();
        String artist = (String) trackInformation.keySet().toArray()[0];

        HashMap<String, List<File>> albumsTracks = trackInformation.get(artist);
        int numAlbums = albumsTracks.size();


        String choice=null;
        List<String> artists, albumNames;
        List<File>currentFiles;
        albumNames = albumsTracks.keySet().stream().toList();

        if (numAlbums == 1) {
            currentFiles = albumsTracks.get(albumNames.get(0));
            albums.add(new Album(artist, albumNames.get(0), currentFiles));
        } else {
            if(disambiguate) {
                choice = useChooser(directory, "Album", albumNames);
            }
            if (choice != null) {
                currentFiles = new ArrayList<>();
                for (String albumName : albumNames) {
                    currentFiles.addAll(albumsTracks.get(albumName));
                }
                for (File file : directory.toFile().listFiles()) {
                    if (!file.isDirectory() && file.getName().endsWith(".mp3")) {
                        Mp3FileUtil.updateFileTags(file.toPath(), choice, null, null, null, null);
                    }
                }
            } else {
                for (String albumName : albumsTracks.keySet()) {
                    currentFiles = albumsTracks.get(albumName);
                    albums.add(new Album(artist, albumName, currentFiles));
                }
            }
        }
        return albums;
    }

    private List<Album> getAlbumsWhenMultipleArtists(Path directory, HashMap<String, HashMap<String, List<File>>> trackInformation
            , List<String> tagArtists, int numFiles,boolean disambiguate) throws IOException {
        List<Album> albums = new ArrayList<>();


        HashMap<String, List<File>> albumsFiles;
        List<File> currentFiles;

        String choice = null;

        if (thisIsADirHoldingVariousArtists(directory)&&disambiguate) {
            choice = useChooser(directory, "Artist", tagArtists);
        }
        if (choice != null) {
            for (File file : directory.toFile().listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(".mp3")) {
                    Mp3FileUtil.updateFileTags(file.toPath(), null, choice, null, null, null);
                }
            }
            albums = getAlbumsFromDirectory(directory,disambiguate);
        } else {
            for (String artist : tagArtists) {
                albumsFiles = trackInformation.get(artist);
                for (String albumName : albumsFiles.keySet()) {
                    currentFiles = albumsFiles.get(albumName);
                    albums.add(new Album(artist, albumName, currentFiles));
                }
            }
        }
        return albums;
    }

    private static boolean thisIsADirHoldingVariousArtists(Path directory) {
        List<String> miscArtistsDirectories = List.of("various artists","va");
        boolean parentIsVA=miscArtistsDirectories.contains(directory.getParent().getFileName().toString().toLowerCase());
        boolean weAreVA=miscArtistsDirectories.contains(directory.getFileName().toString().toLowerCase());
        boolean grandParentIsVA=false;
        if(directory.getParent().getParent() != null){
            grandParentIsVA=miscArtistsDirectories.contains(directory.getParent().getParent().getFileName().toString().toLowerCase());
        }
        return !(parentIsVA||weAreVA||grandParentIsVA);
    }

    private List<Album> getAlbumsFromDirectory(Path directory,boolean disambiguate) throws IOException {
        List<Album> albums = new ArrayList<>();
        String track, album, artist;
        Mp3File mp3;
        HashMap<String, HashMap<String, List<File>>> trackInformation = new HashMap<>();
        System.err.println("Looking at albums in " + directory.toAbsolutePath().toString());
        ID3v1 id3v1Tag;
        ID3v2 id3v2Tag;
        HashMap<String, List<File>> albumsFiles;
        List<File> currentFiles;
        int numFiles = 0;
        if (directory != null && directory.toFile() != null && directory.toFile().listFiles() != null) {
            for (File file : directory.toFile().listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(".mp3")) {
                    try {
                        mp3 = new Mp3File(file);
                        id3v2Tag = mp3.getId3v2Tag();
                        id3v1Tag = mp3.getId3v1Tag();

                        if (id3v2Tag != null) {
                            album = id3v2Tag.getAlbum();
                            artist = id3v2Tag.getArtist();
                            track = id3v2Tag.getTitle();
                        }else if (id3v1Tag != null) {
                            album = id3v1Tag.getAlbum();
                            artist = id3v1Tag.getArtist();
                            track = id3v1Tag.getTitle();
                        }else {
                                System.err.println("There should be no untagged files by now, but " + file.getName() + " has no tag");
                                continue;
                        }

                        if (trackInformation.containsKey(artist)) {
                            albumsFiles = trackInformation.get(artist);
                        } else {
                            albumsFiles = new HashMap<>();
                        }

                        if (albumsFiles.containsKey(album)) {
                            currentFiles = albumsFiles.get(album);
                        } else {
                            currentFiles = new ArrayList<>();
                        }

                        currentFiles.add(file);
                        albumsFiles.put(album, currentFiles);
                        trackInformation.put(artist, albumsFiles);
                        numFiles++;

                    } catch (UnsupportedTagException e) {
                        //skip
                    } catch (InvalidDataException e) {
                        System.err.println("There is something wrong with " + file.getName());
                    }
                }
            }
        }

        List<String> tagArtists = trackInformation.keySet().stream().toList();
        if (tagArtists.size() == 1) {
            albums.addAll(getAlbumsWhenOneArtist(directory, trackInformation, numFiles,disambiguate));
        } else if (tagArtists.size() > 1) {
            albums.addAll(getAlbumsWhenMultipleArtists(directory, trackInformation, tagArtists, numFiles,disambiguate));
        }
        return albums;
    }


}
