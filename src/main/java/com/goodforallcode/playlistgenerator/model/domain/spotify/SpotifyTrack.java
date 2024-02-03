package com.goodforallcode.playlistgenerator.model.domain.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {

    String name;
    String id;

    @JsonProperty("duration_ms")
    int duration;

    @JsonProperty("track_number")
    int trackNumber;

    SpotifyAlbumItem album;
    int popularity;

    @JsonProperty("is_local")
    boolean local;
    @JsonProperty("track")
    boolean track;

    @JsonProperty("href")
    String url;

    List<SpotifyArtist>artists;

}
