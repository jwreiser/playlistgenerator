package com.goodforallcode.playlistgenerator.util;

import com.goodforallcode.playlistgenerator.playlistgenerator.util.FileNameUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileNameUtilTests {

    @Test
    void testDescriptionInclude() {
        Assertions.assertEquals(1, FileNameUtil.getTrackNumberFromName("1 - Nomathemba"));
    }

    @Test
    void testGetCleanFileName() {
        Assertions.assertEquals("Nomathemba",FileNameUtil.getCleanFileName("01 - Nomathemba.mp3"));
    }
}
