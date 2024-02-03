package com.goodforallcode.playlistgenerator.model.domain.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data

public class Release {
    int count;
    String title;
    String status;
    @JsonProperty ("artist-credit")
    List<ArtistCredit> artistCredits;
    @JsonProperty ("track-count")
    int trackCount;

    List<Media> media=new ArrayList<>();
}
