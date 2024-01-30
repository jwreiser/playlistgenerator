module com.goodforallcode.playlistgenerator {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;
    requires mp3agic;
    requires com.fasterxml.jackson.databind;
    requires lombok;
    requires org.jetbrains.annotations;
    requires org.apache.commons.text;


    opens com.goodforallcode.playlistgenerator.playlistgenerator to javafx.fxml;
    exports com.goodforallcode.playlistgenerator.playlistgenerator;
    exports com.goodforallcode.playlistgenerator.playlistgenerator.model.rest.musicbrainz;
}