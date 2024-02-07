package com.goodforallcode.playlistgenerator.playlistgenerator.util;

import com.goodforallcode.playlistgenerator.model.domain.Album;
import com.goodforallcode.playlistgenerator.model.domain.spotify.SpotifyTrack;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.mpatric.mp3agic.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Mp3FileUtil {
    public static Long getMp3Duration(Path file) {
        Long duration=null;
        try {
            Mp3File mp3=new Mp3File(file);
            duration=mp3.getLengthInMilliseconds();
        } catch (IOException|UnsupportedTagException|InvalidDataException e) {
            //leave as null
        }
        return duration;
    }

    public static Mp3Info getMp3Info(Path file) {
        Mp3Info result=null;
        try {
            Mp3File mp3=new Mp3File(file);
            if(mp3.getId3v2Tag()!=null) {
                ID3v2 tag = mp3.getId3v2Tag();
                result=new Mp3Info(tag.getTitle(), tag.getArtist(), tag.getAlbum());
            } else if (mp3.getId3v1Tag()!=null) {
                ID3v1 tag = mp3.getId3v1Tag();
                result=new Mp3Info(tag.getTitle(), tag.getArtist(), tag.getAlbum());
            }
        } catch (IOException|UnsupportedTagException|InvalidDataException e) {
            //leave as null
        }
        if(result!=null){
            if(result.getTitle()!=null && result.getTitle().toLowerCase().startsWith("track")){
                result=new Mp3Info(result.getTitle(),true);//at least the track information is wrong
            }
        }
        return result;
    }

    public static void updateFileTags(Path file,String album,String artist,String trackName,Integer genreCode,String genre) {
        try {
            Mp3File mp3 = new Mp3File(file);
            mp3.setId3v2Tag(null);
            ID3v1 tag = getTag(mp3);
            if(tag==null){
                tag=new ID3v1Tag();
            }

            if(album!=null) {
                tag.setAlbum(album);
            }
            if(artist!=null) {
                tag.setArtist(artist);
            }
            if(trackName!=null) {
                tag.setTitle(trackName);
            }

            if (genreCode != null) {
                tag.setGenre(genreCode);
            } else if (genre!=null) {
                genreCode = ID3v1Genres.matchGenreDescription(genre);
                tag.setGenre(genreCode);
            }
            mp3.setId3v1Tag(tag);
            saveMp3(file,mp3);
        } catch (IOException | UnsupportedTagException e) {
            //dont write new tags
        }catch (InvalidDataException e2){
            System.err.println("There is a problem with the file at "+file.toAbsolutePath().toString());
        }
    }

    public static  ID3v1 getTag(Mp3File mp3){
        ID3v1 tag=null;
        if(mp3.getId3v1Tag()!=null){
            tag = mp3.getId3v1Tag();
            //for some reason v1 tags sometimes don't return the full comment and if they miss retry we run forever
            if(mp3.getId3v2Tag()!=null){
                ID3v2 v2Tag=mp3.getId3v2Tag();
                if(v2Tag.getComment()!=null && v2Tag.getComment().contains(tag.getComment())){
                    tag.setComment(v2Tag.getComment());
                }
            }
        }else if(mp3.getId3v2Tag()!=null){
            tag = mp3.getId3v2Tag();
        }
        return tag;
    }
    public static boolean saveMp3(Path file, Mp3File mp3) {
        String fileName = getSavingFileName(file.toAbsolutePath().toString());
        boolean success=false;
        try {
            //move tag to v1 if it is not there
            ID3v1 tag=getTag(mp3);
            mp3.setId3v2Tag(convertTag(tag));
            mp3.setId3v1Tag(tag);

            mp3.save(fileName);
            file.toFile().delete();

            if(fileName.contains("auto tagged")) {
                File recentlyCreatedFile=new File(fileName);
                if(recentlyCreatedFile!=null) {
                    mp3=new Mp3File(recentlyCreatedFile);
                    fileName = getSavingFileName(fileName);
                    mp3.save(fileName);
                    recentlyCreatedFile.delete();
                }

            }
            success=true;
        } catch (IOException  |InvalidDataException|UnsupportedTagException e) {
            throw new RuntimeException(e);
        }catch (NotSupportedException nse){
            //there is generally nothing we can do here and generally we just need to skip the file
            System.err.println("There is a problem with the file at "+file.getFileName());
        }
        return success;
    }

    @NotNull
    /**
     *This alternates between an original name and a tagged name so that we can delete twice and get back to the original file name
     * In case something happens we want to leave a meaningful file name
     */
    public static String getSavingFileName(String fileName) {
        if(!fileName.contains("auto tagged")) {
            fileName=fileName.replace(".mp3", "(auto tagged).mp3");
        }else{
            fileName=fileName.replace("(auto tagged)", "");
        }
        return fileName;
    }

    public static ID3v2 convertTag(ID3v1 source){
        ID3v2 result=new ID3v23Tag();
        if(source.getGenre()>0) {
            result.setGenre(source.getGenre());
            try {
                result.setGenreDescription(source.getGenreDescription());
            }catch (IllegalArgumentException ie){
                //genre's not important leave it out
            }
        }
        result.setTitle(source.getTitle());
        result.setArtist(source.getArtist());
        result.setAlbum(source.getAlbum());
        result.setAlbumArtist(source.getArtist());
        result.setComment(source.getComment());
        result.setTrack(source.getTrack());
        result.setYear(source.getYear());
        return result;
    }

    public static boolean doesEveryFileHaveSpotifyInformation(Album album) {
        boolean result = true;

        for (File file : album.getFiles()) {
            result = fileHasSpotifyInformation(file);
            if(!result){
                break;
            }
        }

        return result;
    }

    public static boolean wasEveryFilePublished(Path directory) {
        boolean result = true;

        for (File file : directory.toFile().listFiles()) {
            if(file.isFile()&&file.getName().endsWith(".mp3")) {
                result = fileWasPublishedOrCantBe(file);
                if (!result) {
                    break;
                }
            }
        }

        return result;
    }

    public static boolean wasEveryFilePublished(Album album) {
        boolean result = true;

        for (File file : album.getFiles()) {
            result = fileWasPublishedOrCantBe(file);
            if(!result){
                break;
            }
        }

        return result;
    }

    public static boolean fileHasSpotifyInformation(File file) {
        boolean result=false;
        try {
            Mp3File mp3 = new Mp3File(file);

            ID3v1 tag = getTag(mp3);
            if (tag != null && tag.getComment()!=null && ( tag.getComment().contains("spotifyTrackId") ||tag.getComment().contains("spotifyExistenceCheckFailed"))) {
                result = true;
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();//This should never happen as we should have filtered out bad files by here, but if it happens continue on
        }
        return result;
    }

    public static boolean fileWasPublishedOrCantBe(File file) {
        boolean result=false;
        try {
            Mp3File mp3 = new Mp3File(file);

            ID3v1 tag = getTag(mp3);
            if (tag != null && tag.getComment()!=null && ( tag.getComment().contains("spotifyTrackUploaded") ||
                    tag.getComment().contains("spotifyUploadFailure") ||tag.getComment().contains("spotifyExistenceCheckFailed"))) {
                result = true;
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException |IllegalArgumentException e) {
            e.printStackTrace();//This should never happen as we should have filtered out bad files by here, but if it happens continue on
        }
        return result;
    }

    public static boolean currentlyRetrying(File file) {
        boolean result=false;
        try {
            Mp3File mp3 = new Mp3File(file);

            ID3v1 tag = getTag(mp3);
            if (tag != null && tag.getComment()!=null &&  tag.getComment().contains("spotifyRetry")){
                result = true;
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();//This should never happen as we should have filtered out bad files by here, but if it happens continue on
        }
        return result;
    }

    public static boolean addCommentToFile(File file) {
        boolean success = false;

        Mp3File mp3 = null;
        try {
            mp3 = new Mp3File(file);
        ID3v1 tag = getTag(mp3);
        String comment = tag.getComment();
        if (!comment.isEmpty()) {
            comment = comment + ";spotifyTrackUploaded:true";
        } else {
            comment = "spotifyTrackUploaded";
        }
        tag.setComment(comment);
        Mp3FileUtil.saveMp3(file.toPath(), mp3);
        success=true;
        } catch (IOException|UnsupportedTagException|InvalidDataException e) {
            throw new RuntimeException(e);
        }
        return success;
    }

    public static boolean setStatusToUploadFailed(File file){
        boolean success=false;
        try {
            Mp3File mp3=new Mp3File(file);
            ID3v1 tag =Mp3FileUtil.getTag(mp3);
            tag.setComment("spotifyUploadFailure");
            Mp3FileUtil.saveMp3(file.toPath(),mp3);
            success=true;
        } catch (IOException |UnsupportedTagException|InvalidDataException ex) {

        }
        return success;
    }

    public static boolean setStatusToRetry(File file){
        boolean success=false;
        try {
            Mp3File mp3=new Mp3File(file);
            ID3v1 tag =Mp3FileUtil.getTag(mp3);
            tag.setComment("spotifyRetry");
            Mp3FileUtil.saveMp3(file.toPath(),mp3);
            success=true;
        } catch (IOException |UnsupportedTagException|InvalidDataException ex) {

        }
        return success;
    }

    public static boolean addTrackInformation(ID3v1 tag, SpotifyTrack track,File file,Mp3File mp3){
        boolean success=false;
        boolean retry=false;
        String comment;

        if(tag.getComment()!=null && tag.getComment().contains("spotifyRetry")){
            retry=true;
        }
        comment = "spotifyTrackId:" + track.getId();
        if (track.getAlbum() != null) {
            comment += "; spotifyAlbumId:" + track.getAlbum().getId();
        }
        if(retry){
            comment+=";spotifyRetry";
        }
        tag.setComment(comment);

        mp3.setId3v2Tag(null);
        mp3.setId3v1Tag(tag);
        success = Mp3FileUtil.saveMp3(file.toPath(), mp3);
        return success;
    }

}
