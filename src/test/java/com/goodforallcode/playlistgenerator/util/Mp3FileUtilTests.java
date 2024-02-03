package com.goodforallcode.playlistgenerator.util;

import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class Mp3FileUtilTests {


    @Test
    void testGetSavingFileName() {
        Assertions.assertEquals("Lizzie.mp3",Mp3FileUtil.getSavingFileName("Lizzie(auto tagged).mp3"));
        Assertions.assertEquals("Lizzie(auto tagged).mp3",Mp3FileUtil.getSavingFileName("Lizzie.mp3"));
    }

    @Test
    void testSaveMp3() throws InvalidDataException, UnsupportedTagException, IOException {
        String path="C:\\Users\\justi\\OneDrive\\Desktop\\backup\\music\\Burned\\spacey electronic and world\\The Orb\\The Orb's Adventures Beyond the Ultraworld/Outlands.mp3";
        Path pathFile=Path.of(path);
        Mp3File mp3=new Mp3File(pathFile);
        Assertions.assertTrue(Mp3FileUtil.saveMp3(pathFile,mp3));
    }

}
