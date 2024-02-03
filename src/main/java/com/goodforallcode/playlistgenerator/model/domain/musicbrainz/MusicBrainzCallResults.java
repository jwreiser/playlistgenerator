package com.goodforallcode.playlistgenerator.model.domain.musicbrainz;

import org.apache.commons.text.similarity.LevenshteinDistance;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.mpatric.mp3agic.ID3v1Genres;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data

public class MusicBrainzCallResults {
    static LevenshteinDistance distanceCalculator=LevenshteinDistance.getDefaultInstance();
    int count;
    List<Recording> recordings;

    public MusicBrainzCallResults() {
        String test;
    }

    public Mp3Info getMp3InfoFromResults(){
        Mp3Info results=null;
        if(recordings.size()>0) {
            Recording recordingToUse = recordings.get(0);
            Release releaseToUse=null;
            if (recordingToUse.getReleases().size() > 0) {
                releaseToUse = recordingToUse.getReleases().get(0);
            }
            results = getMp3Info(recordingToUse, releaseToUse);
        }
        return results;
    }

    @NotNull
    private static Mp3Info getMp3Info(Recording recordingToUse, Release releaseToUse) {
        Mp3Info results;
        String title = recordingToUse.getTitle();
        String topGenreDescription = null;
        int topCount = 1;
        int topGenre = 0, currentGenre;
        if(recordingToUse.getTags()!=null) {
            for (Tag tag : recordingToUse.getTags()) {
                if (tag.getCount() > topCount) {
                    currentGenre = ID3v1Genres.matchGenreDescription(tag.getName());
                    if (currentGenre > 0) {
                        topGenreDescription = tag.getName();
                        topCount = tag.getCount();
                        topGenre = currentGenre;
                    }
                }
            }
        }
        String artist = null;
        if (recordingToUse.getArtistCredits().size() > 0) {
            artist = recordingToUse.getArtistCredits().get(0).getName();
        }
        String album = null;
        if (releaseToUse !=null) {
            album = releaseToUse.getTitle();
            //trust these over recording artists
            if(releaseToUse.getArtistCredits()!=null && releaseToUse.getArtistCredits().size()>0){
                artist= releaseToUse.getArtistCredits().get(0).getName();
            }
        }
        if (topGenreDescription != null) {
            results = new Mp3Info(title, artist, album, topGenre, topGenreDescription);
        } else {
            results = new Mp3Info(title, artist, album);
        }
        return results;
    }

    public Mp3Info getMp3InfoFromResults(String suggestedTrack,Integer trackNumber,Integer numberOfTracks, long duration){
        Results results=getElementsToUse(suggestedTrack,trackNumber,numberOfTracks,duration,false);

        if(results.getRecordingToUse()==null|| results.getReleaseToUse()==null){
            results=getElementsToUse(suggestedTrack,trackNumber,numberOfTracks,duration,true);
            if(results.getRecordingToUse()==null|| results.getReleaseToUse()==null) {
                return null;
            }else{
                return getMp3Info(results.getRecordingToUse(), results.getReleaseToUse());
            }
        }else {
            return getMp3Info(results.getRecordingToUse(), results.getReleaseToUse());
        }
    }
    @Data
    @AllArgsConstructor
    private class Results {
        Recording recordingToUse = null;
        Release releaseToUse = null;
    }
    private Results getElementsToUse(String suggestedTrack,Integer trackNumber,Integer numberOfTracks, long duration,boolean assumeTrackCountIncorrect) {
        Recording recordingToUse = null;
        Release releaseToUse = null;
        long bestDifference = Integer.MAX_VALUE;
        long currentDuration, currentDifference;

        boolean bestTitleIsASubset=false;
        int bestDistance=Integer.MAX_VALUE,currentDistance;
        String currentTrack;
        suggestedTrack=suggestedTrack.replace(trackNumber+"","").replace("&","and").trim().toLowerCase();

        if (recordings.size() > 0) {
            for (Recording recording : recordings) {
                if(!suggestedTrack.isEmpty()) {
                    currentTrack = recording.getTitle().replace("&","and").toLowerCase();
                    if (suggestedTrack.contains(currentTrack)) {
                        currentDistance = distanceCalculator.apply(suggestedTrack, currentTrack);
                        if (bestTitleIsASubset) {
                            if (currentDistance > bestDistance) {
                                continue;
                            }
                        }
                        bestTitleIsASubset = true;
                        bestDistance = currentDistance;
                        recordingToUse=recording;//at least use this if we can't find a release
                    } else {
                        if (bestTitleIsASubset) {
                            continue;
                        } else {
                            currentDistance = distanceCalculator.apply(suggestedTrack, currentTrack);
                            if (currentDistance > bestDistance) {
                                continue;
                            } else {
                                bestDistance = currentDistance;
                            }
                        }
                    }
                }

                if(recording.getReleases()!=null) {
                    for (Release release : recording.getReleases()) {
                        if ((release.getTrackCount() == numberOfTracks || (assumeTrackCountIncorrect && release.getTrackCount() > numberOfTracks)) && mediaIsRightTrack(release, trackNumber)) {

                            //TODO see if possible to get more than one back
                            currentDuration = release.getMedia().get(0).getTrack().get(0).getLength();
                            if (releaseToUse != null) {
                                currentDifference = Math.abs(duration - currentDuration);
                                if (currentDifference < bestDifference) {
                                    releaseToUse = release;
                                    bestDifference = Math.abs(duration - currentDuration);
                                    recordingToUse = recording;
                                }
                            } else {
                                releaseToUse = release;
                                bestDifference = Math.abs(duration - currentDuration);
                                recordingToUse = recording;
                            }
                        }
                    }
                }
            }
        }
        return new Results(recordingToUse,releaseToUse);
    }
    private boolean mediaIsRightTrack(Release release,Integer trackNumber){
        boolean hasMedia= release.getMedia()!=null && release.getMedia().size()==1;
        if(hasMedia) {
            Media media=release.getMedia().get(0);
            //TODO replace the CD assumption with input
            if(media.getFormat()!=null && media.getFormat().toUpperCase().equals("CD")&& media.getTrackOffset() == (trackNumber - 1)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }

    }
}
