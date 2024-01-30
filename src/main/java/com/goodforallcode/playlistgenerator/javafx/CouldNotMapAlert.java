package com.goodforallcode.playlistgenerator.javafx;

import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class CouldNotMapAlert {
    Path file;
    String filePath="";
    String artist="";
    String album="";
    String trackName="";

    public boolean show(){
        Stage stage=new Stage();

        TextField fileInput=new TextField();
        fileInput.appendText(filePath);
        fileInput.setEditable(false);

        Label filePathLabel=new Label();
        filePathLabel.setText("Path");
        filePathLabel.setLabelFor(fileInput);


        TextField artistInput=new TextField();
        artistInput.appendText(artist);

        Label artistLabel=new Label();
        artistLabel.setText("Artist");
        artistLabel.setLabelFor(artistInput);

        TextField albumInput=new TextField();
        albumInput.appendText(album);

        Label albumLabel=new Label();
        albumLabel.setText("Album");
        albumLabel.setLabelFor(albumInput);

        TextField trackNameInput=new TextField();
        trackNameInput.appendText(trackName);

        Label trackNameLabel=new Label();
        trackNameLabel.setText("Track Name");
        trackNameLabel.setLabelFor(trackNameInput);

        TextField genreInput=new TextField();
        trackNameInput.setEditable(true);

        Label genreLabel=new Label();
        genreLabel.setText("Genre");
        genreLabel.setLabelFor(genreInput);

        Button saveButton=new Button("Save MP3 Tags");
        saveButton.setOnAction(e-> {Mp3FileUtil.updateFileTags(file,albumInput.getText(),artistInput.getText(),trackNameInput.getText(),null,genreInput.getText());stage.close();});
        VBox pane=new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getChildren().addAll(filePathLabel,fileInput,artistLabel,artistInput,albumLabel,albumInput,trackNameLabel,trackNameInput,genreLabel,genreInput,saveButton);

        Scene scene=new Scene(pane,400,400);
        stage.setScene(scene);

        stage.setTitle("Could not map!");
        stage.showAndWait();
        return true;
    }
}
