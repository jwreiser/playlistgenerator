package com.goodforallcode.playlistgenerator.playlistgenerator.service;

import com.goodforallcode.playlistgenerator.javafx.CouldNotMapAlert;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.FileNameUtil;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.goodforallcode.playlistgenerator.playlistgenerator.util.StringUtil;
import javafx.scene.control.ProgressBar;
import org.apache.commons.lang3.math.NumberUtils;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.rest.musicbrainz.MusicBrainzCallResults;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.MusicBrainzRestCaller;
import com.mpatric.mp3agic.*;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil.updateFileTags;

public class SongInformationService {
    static MusicBrainzRestCaller caller = new MusicBrainzRestCaller();

    public Set<Mp3Info> getSongInformation(String currentDirectory, Set<String> visitedDirectories, Set<String> remainingDirectories, ProgressBar progress) throws IOException {
        Set<Mp3Info> fileList = new HashSet<>();
        visitedDirectories.add(currentDirectory);
        final long numFiles=getNumberOfSongs(currentDirectory);
        final double share=1.0/numFiles;
        Files.walkFileTree(Paths.get(currentDirectory), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                System.err.println("Visiting " + file.toAbsolutePath());
                if (!Files.isDirectory(file)) {
                    progress.setProgress(progress.getProgress()+share);
                    if (file.getFileName().toString().endsWith("mp3")) {
                        Mp3Info info = fileToInfo(file);
                        if (info != null) {
                            fileList.add(info);
                        }
                    }
                } else {
                    if (!visitedDirectories.contains(file.getFileName().toString())) {
                        remainingDirectories.add(file.getFileName().toString());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }
    public long getNumberOfSongs(String currentDirectory) throws IOException {
        Path directory=Path.of(currentDirectory);
        return Files.walk(directory).parallel().filter(p -> !p.toFile().isDirectory() && p.getFileName().toString().endsWith(".mp3")).count();
    }

    private Mp3Info fileToInfo(Path file) {
        Mp3Info result = Mp3FileUtil.getMp3Info(file);
        String album = StringUtil.cleanupAlbum(file.getParent().getFileName().toString());
        String trackName = FileNameUtil.getCleanFileName(file);
        if(trackName.endsWith(" UPDATED")){
            return null;
        }
        String artist = null;
        Integer trackNumber= null;
        Integer numTracks= null;
        if (List.of("misc", "mixed", "various", "various artists").contains(album.toLowerCase()) && trackName.contains(" - ")) {
            String testTrack=trackName = FileNameUtil.getCleanFileName(file,true);
            String[] parts = trackName.split(" - ");
            if (parts.length == 2) {
                trackName = parts[0];
                artist = parts[1];
                album = null;
            }
        } else {
            artist = file.getParent().getParent().getFileName().toString();
        }
        if(result!=null && result.isContainsTrackInfo()){
            numTracks=0;
            for(File currentFile:file.getParent().toFile().listFiles()){
                if(currentFile.getName().endsWith(".mp3")&&!currentFile.getName().endsWith("UPDATED.mp3")){
                    numTracks++;
                }
            }
            String trackNumString=result.getTitle().toLowerCase().replace("track","").replaceAll(" ","");
            if(NumberUtils.isNumber(trackNumString)){
                trackNumber=Integer.parseInt(trackNumString);
            }
            lookupMp3Tags(file,trackNumber, numTracks,album, trackName, artist);

        }
        else if (result == null) {
            lookupMp3Tags(file,trackNumber, numTracks,album, trackName, artist);
        }
        return result;
    }

    private Mp3Info lookupMp3Tags(Path file,Integer tracknum, Integer numTracks,String album,String trackName, String artist) {
        Long duration = Mp3FileUtil.getMp3Duration(file);


        Mp3Info result =null;
        if(weHaveBadTagData(tracknum, numTracks)) {
            result=getMp3Info(trackName,tracknum, numTracks, album, artist,duration);
        }else{
            result=getMp3Info(file,trackName, album, artist);
        }
        if (result != null) {
            Integer genre=null;
            if (result.getGenreDescription() != null) {
                genre=result.getGenre();
            }
            updateFileTags(file, result.getAlbum(),result.getArtist(),result.getTitle(),genre,null);
        } else {
            //use existing file information since it seems like it is all we are going to get
            String existingTrackName=null;
            if(trackName!=null){
                existingTrackName=trackName.toLowerCase().replace("track","").replaceAll("\\d","");
            }
            if(weHaveBadTagData(tracknum, numTracks) &&existingTrackName!=null  && existingTrackName.length()>5) {
                updateFileTags(file, album,artist,trackName,null,null);
            }else {
                CouldNotMapAlert alert=new CouldNotMapAlert(file,file.toAbsolutePath().toString(),artist ,album ,file.getFileName().toString().replace(".mp3",""));
                alert.show();
            }
        }


        return result;
    }



    private static boolean weHaveBadTagData(Integer tracknum, Integer numTracks) {
        return tracknum != null && numTracks != null;
    }

    private static Mp3Info getMp3Info(String suggestedTrack,Integer tracknum, Integer numTracks, String album, String artist,long duration){
        MusicBrainzCallResults results  = null;
        if(suggestedTrack!=null){
            suggestedTrack=suggestedTrack.toLowerCase().replace("track","");
            if(suggestedTrack.length()>5) {
                results = caller.getInfoFromArtistAndTrack(suggestedTrack, artist, "jreiser.is@gmail.com");
            }
        }
        if(results==null) {
            results = caller.getInfoFromArtistAndAlbum(artist, album, "jreiser.is@gmail.com");
        }
        Mp3Info result=null;
        if(results!=null){
            result=results.getMp3InfoFromResults(suggestedTrack,tracknum,numTracks,duration);
        }
        return result;
    }
    private static Mp3Info getMp3Info(Path file,String trackName, String album, String artist) {
        MusicBrainzCallResults results = null;
        Mp3Info result = null;

        if (album != null) {
            results = caller.getInfoFromFullInformation(trackName, album, artist, "jreiser.is@gmail.com");
        } else {
            results = caller.getInfoFromArtistAndTrack(trackName, artist, "jreiser.is@gmail.com");
        }

        if (results != null) {
            result = results.getMp3InfoFromResults();
        }
        if (result == null) {
            if (album != null) {
                results = caller.getInfoFromAlbum(trackName, album, "jreiser.is@gmail.com");
                result = results.getMp3InfoFromResults();
                if (result == null) {//since album is not null we did not try this yet
                    results = caller.getInfoFromArtistAndTrack(trackName, artist, "jreiser.is@gmail.com");
                    result = results.getMp3InfoFromResults();
                }
            }

        }
        if (result == null) {
            results = caller.getInfoFromTrack(trackName, "jreiser.is@gmail.com");
            if (results.getCount() == 1) {
                result = results.getMp3InfoFromResults();
            } else {
                CouldNotMapAlert alert=new CouldNotMapAlert(file,file.toAbsolutePath().toString(),artist ,album ,file.getFileName().toString().replace(".mp3",""));
                alert.show();
            }
        }
        return result;
    }
}
