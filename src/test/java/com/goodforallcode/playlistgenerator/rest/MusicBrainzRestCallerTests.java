package com.goodforallcode.playlistgenerator.rest;

import com.goodforallcode.playlistgenerator.model.domain.musicbrainz.MusicBrainzCallResults;
import com.goodforallcode.playlistgenerator.playlistgenerator.rest.MusicBrainzRestCaller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MusicBrainzRestCallerTests {
    MusicBrainzRestCaller caller=new MusicBrainzRestCaller();
    @Test
    void testGetInfoFromRecordingWorksWithEncoded() {
        assertNotNull(caller.getInfoFromFullInformation("thanx","40%20Oz.%20to%20Freedom","Sublime","jreiser.is@gmail.com"));
    }

    @Test
    void testGetInfoFromRecording() {
        MusicBrainzCallResults infoFromRecording = caller.getInfoFromFullInformation("thanx", "40 Oz. to Freedom", "Sublime", "jreiser.is@gmail.com");
        assertNotNull(infoFromRecording);
        assertEquals(2,infoFromRecording.getCount());
    }
}
