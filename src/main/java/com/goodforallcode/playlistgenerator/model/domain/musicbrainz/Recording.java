package com.goodforallcode.playlistgenerator.model.domain.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data

public class Recording {
    int score;
    String title;
    int length;
    @JsonProperty ("artist-credit")
    List<ArtistCredit> artistCredits;
    String disambiguation;
    List<Release> releases;
    List<Tag> tags;

}
