package com.goodforallcode.playlistgenerator.model.domain.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrackSearchResults {

    @JsonProperty("tracks")
    SpotifyTracksContainer tracksContainer;
}
