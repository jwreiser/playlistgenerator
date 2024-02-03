package com.goodforallcode.playlistgenerator.model.domain.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyAlbumItem {

    @JsonProperty("album_type")
    String albumType;
    List<SpotifyArtist>artists;
    String id;
    String name;
    int popularity;
    @JsonProperty("total_tracks")
    int totalTracks;

    @JsonProperty("tracks")
    SpotifyTracksContainer tracksContainer;

}
