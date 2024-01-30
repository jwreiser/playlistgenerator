package com.goodforallcode.playlistgenerator.playlistgenerator.model.rest.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ArtistCredit {
    String name;
}
