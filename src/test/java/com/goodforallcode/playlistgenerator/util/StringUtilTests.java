package com.goodforallcode.playlistgenerator.util;

import com.goodforallcode.playlistgenerator.playlistgenerator.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilTests {

    @Test
    void testCleanupTrack() {
        Assertions.assertEquals("nodiggity", StringUtil.cleanupTrack("No Diggity(feat. Dr Dre)"));
        Assertions.assertEquals("nodiggity", StringUtil.cleanupTrack("No Diggity[feat. Dr Dre]"));
        Assertions.assertEquals("sisterbrother", StringUtil.cleanupTrack("sister, brother"));
        Assertions.assertEquals("letmeclearmythroat", StringUtil.cleanupTrack("Let Me Clear My Throat - Old School Reunion Remix '96"));
    }

    @Test
    void testCleanupArtist() {
        Assertions.assertEquals("ceu", StringUtil.cleanupArtist("Céu"));

    }
}
