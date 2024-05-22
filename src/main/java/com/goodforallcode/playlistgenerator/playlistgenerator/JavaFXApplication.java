package com.goodforallcode.playlistgenerator.playlistgenerator;

import com.goodforallcode.playlistgenerator.javafx.PlaylistFileGeneratingTask;
import com.goodforallcode.playlistgenerator.javafx.PlaylistGeneratingTask;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.MusicBrainzAlbumService;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.SongInformationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class JavaFXApplication extends Application {
    static SongInformationService songService =new SongInformationService();
    static MusicBrainzAlbumService musicBrainzAlbumService =new MusicBrainzAlbumService();

    public static void main(String[] args) {
        launch();
    }
    Stage stage;
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JavaFXApplication.class.getResource("hello-view.fxml"));

        stage=primaryStage;

        TextField directoryInput=new TextField();

        Button directoryButton=new Button();
        directoryButton.setText("Choose Music Directory");
        directoryButton.setOnAction(e->chooseDirectory(primaryStage,directoryInput));

        Label directoryLabel=new Label();
        directoryLabel.setText("Directory");
        directoryLabel.setLabelFor(directoryInput);

        TextField usernameInput=new TextField();

        Label usernameLabel=new Label();
        usernameLabel.setText("Spotify UserName");
        usernameLabel.setLabelFor(usernameInput);

        TextField playlistInput=new TextField();

        Label playlistLabel=new Label();
        playlistLabel.setText("Playlist Name");
        playlistLabel.setLabelFor(playlistInput);

        CheckBox tagsCheckBox = new CheckBox("Manually Configure Tags");
        CheckBox disambiguateCheckBox = new CheckBox("Manually Disambiguate Artists/Albums");

        ProgressBar progress = new ProgressBar(0);

        Button filePlaylistButton=new Button();
        filePlaylistButton.setText("1. Generate Playlist File");
        filePlaylistButton.setOnAction(e->generatePlaylistFile(directoryInput.getText(),progress,usernameInput.getText(),playlistInput.getText(),tagsCheckBox.isSelected(),disambiguateCheckBox.isSelected()));

        Button spotifyPlaylistButton=new Button();
        spotifyPlaylistButton.setText("2. Generate Playlist");
        spotifyPlaylistButton.setOnAction(e->generatePlaylist(directoryInput.getText(),progress,usernameInput.getText(),playlistInput.getText(),tagsCheckBox.isSelected(),disambiguateCheckBox.isSelected()));

        Button closeButton=new Button();
        closeButton.setText("Close");
        closeButton.setOnAction(e->close());
        closeButton.setDisable(true);

        VBox pane=new VBox(15);
        pane.getChildren().addAll(directoryButton,directoryLabel,directoryInput,usernameLabel,usernameInput,playlistLabel,playlistInput,tagsCheckBox,disambiguateCheckBox,progress,
                filePlaylistButton,spotifyPlaylistButton,closeButton);
        pane.setAlignment(Pos.CENTER);

        Scene scene=new Scene(pane,500,500/*vert*/);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Generate Playlist");
        primaryStage.setOnCloseRequest(e->{e.consume();close();});
        primaryStage.show();
    }
    public void chooseDirectory(Stage stage,TextField directoryInput){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Music Directories");

        File defaultDirectory = new File("c:/");
        chooser.setInitialDirectory(defaultDirectory);

        File selectedDirectory = chooser.showDialog(stage);
        directoryInput.setText(selectedDirectory.getAbsolutePath());
    }

    public void chooseFile(Stage stage,TextField fileInput){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Playlist");

        File defaultDirectory = new File("c:/");
        chooser.setInitialDirectory(defaultDirectory);

        File selectedDirectory = chooser.showOpenDialog(stage);
        fileInput.setText(selectedDirectory.getAbsolutePath());
    }
    public void generatePlaylistFile(String directory,ProgressBar progress, String userName,String playlistName,boolean manuallyConfigureMp3Tags,boolean disambiguate){
        PlaylistFileGeneratingTask task = new PlaylistFileGeneratingTask(directory,userName,playlistName,manuallyConfigureMp3Tags,disambiguate);
//        progress.progressProperty().bind(task.progressProperty());
//        task.setProgressBar(progress);
        new Thread(task).start();
//        Platform.runLater(task);
    }
    public void generatePlaylist(String directory,ProgressBar progress, String userName,String playlistName,boolean manuallyConfigureMp3Tags,boolean disambiguate){
        PlaylistGeneratingTask task = new PlaylistGeneratingTask(directory,userName,playlistName,manuallyConfigureMp3Tags,disambiguate);
//        progress.progressProperty().bind(task.progressProperty());
//        task.setProgressBar(progress);
        new Thread(task).start();
//        Platform.runLater(task);
    }

    public void close(){
        stage.close();
    }

}