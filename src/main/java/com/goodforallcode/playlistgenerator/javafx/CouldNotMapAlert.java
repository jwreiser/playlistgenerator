package com.goodforallcode.playlistgenerator.javafx;

import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;

public class CouldNotMapAlert {
    Path file;
    String filePath = "";
    String artist = "";
    String album = "";
    String trackName = "";

    public CouldNotMapAlert(Path file, String filePath, String artist, String album, String trackName) {
        this.file = file;
        this.filePath = filePath;
        this.artist = artist;
        this.album = album;
        this.trackName = trackName;
    }

    public boolean show() {
        Stage stage = new Stage();

        TextField fileInput = new TextField();
        fileInput.appendText(filePath);
        fileInput.setEditable(false);

        Label filePathLabel = new Label();
        filePathLabel.setText("Path");
        filePathLabel.setLabelFor(fileInput);


        TextField artistInput = new TextField();
        artistInput.appendText(artist);

        Label artistLabel = new Label();
        artistLabel.setText("Artist");
        artistLabel.setLabelFor(artistInput);

        TextField albumInput = new TextField();
        albumInput.appendText(album);

        Label albumLabel = new Label();
        albumLabel.setText("Album");
        albumLabel.setLabelFor(albumInput);

        TextField trackNameInput = new TextField();
        trackNameInput.appendText(trackName);

        Label trackNameLabel = new Label();
        trackNameLabel.setText("Track Name");
        trackNameLabel.setLabelFor(trackNameInput);

        TextField genreInput = new TextField();
        trackNameInput.setEditable(true);

        Label genreLabel = new Label();
        genreLabel.setText("Genre");
        genreLabel.setLabelFor(genreInput);

        Button saveButton = new Button("Save MP3 Tags");
        saveButton.setOnAction(e -> {
            Mp3FileUtil.updateFileTags(file, albumInput.getText(), artistInput.getText(), trackNameInput.getText(), null, genreInput.getText());
            stage.close();
        });
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getChildren().addAll(filePathLabel, fileInput, artistLabel, artistInput, albumLabel, albumInput, trackNameLabel, trackNameInput, genreLabel, genreInput, saveButton);

        Scene scene = new Scene(pane, 400, 400);
        stage.setScene(scene);

        stage.setTitle("Could not map!");
        stage.showAndWait();
        return true;
    }

    public Path getFile() {
        return this.file;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CouldNotMapAlert)) return false;
        final CouldNotMapAlert other = (CouldNotMapAlert) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$file = this.getFile();
        final Object other$file = other.getFile();
        if (this$file == null ? other$file != null : !this$file.equals(other$file)) return false;
        final Object this$filePath = this.getFilePath();
        final Object other$filePath = other.getFilePath();
        if (this$filePath == null ? other$filePath != null : !this$filePath.equals(other$filePath)) return false;
        final Object this$artist = this.getArtist();
        final Object other$artist = other.getArtist();
        if (this$artist == null ? other$artist != null : !this$artist.equals(other$artist)) return false;
        final Object this$album = this.getAlbum();
        final Object other$album = other.getAlbum();
        if (this$album == null ? other$album != null : !this$album.equals(other$album)) return false;
        final Object this$trackName = this.getTrackName();
        final Object other$trackName = other.getTrackName();
        if (this$trackName == null ? other$trackName != null : !this$trackName.equals(other$trackName)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CouldNotMapAlert;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $file = this.getFile();
        result = result * PRIME + ($file == null ? 43 : $file.hashCode());
        final Object $filePath = this.getFilePath();
        result = result * PRIME + ($filePath == null ? 43 : $filePath.hashCode());
        final Object $artist = this.getArtist();
        result = result * PRIME + ($artist == null ? 43 : $artist.hashCode());
        final Object $album = this.getAlbum();
        result = result * PRIME + ($album == null ? 43 : $album.hashCode());
        final Object $trackName = this.getTrackName();
        result = result * PRIME + ($trackName == null ? 43 : $trackName.hashCode());
        return result;
    }

    public String toString() {
        return "CouldNotMapAlert(file=" + this.getFile() + ", filePath=" + this.getFilePath() + ", artist=" + this.getArtist() + ", album=" + this.getAlbum() + ", trackName=" + this.getTrackName() + ")";
    }
}
