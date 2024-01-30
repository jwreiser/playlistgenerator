package com.goodforallcode.playlistgenerator.playlistgenerator.util;

public class StringUtil {

    public static String cleanupAlbum(String album){
        album=album.toLowerCase().replaceAll("disc 1","").replaceAll("disc one","").
                replaceAll("disc 2","").replaceAll("disc two","");
        return album;
    }
    

}
