package com.goodforallcode.playlistgenerator.model.domain.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data

public class Media {
    @JsonProperty("track-count")
    int trackCount;
    @JsonProperty("track-offset")
    int trackOffset;
    String format;

    List<Track> track=new ArrayList<>();
}
