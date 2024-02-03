package com.goodforallcode.playlistgenerator.model.domain.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ArtistCredit {
    String name;
}
