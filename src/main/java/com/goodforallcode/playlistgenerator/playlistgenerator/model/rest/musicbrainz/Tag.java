package com.goodforallcode.playlistgenerator.playlistgenerator.model.rest.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data

public class Tag {
    int count;
    String name;
}
