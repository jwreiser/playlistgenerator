package com.goodforallcode.playlistgenerator.playlistgenerator.util;

import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.mpatric.mp3agic.*;

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
            ID3v1 tag = new ID3v1Tag();
            tag.setAlbum(album);
            tag.setArtist(artist);
            tag.setTitle(trackName);
            if (genreCode != null) {
                tag.setGenre(genreCode);
            } else if (genre!=null) {
                genreCode = ID3v1Genres.matchGenreDescription(genre);
                tag.setGenre(genreCode);
            }
            mp3.setId3v1Tag(tag);
            mp3.save(file.toAbsolutePath().toString().replace(".mp3", "(auto tagged).mp3"));
            file.toFile().delete();
        } catch (IOException | UnsupportedTagException | NotSupportedException e) {
            //dont write new tags
        }catch (InvalidDataException e2){
            System.err.println("There is a problem with the file at "+file.toAbsolutePath().toString());
        }
    }
}
