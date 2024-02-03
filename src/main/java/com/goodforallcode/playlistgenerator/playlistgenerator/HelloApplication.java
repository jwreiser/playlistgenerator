package com.goodforallcode.playlistgenerator.playlistgenerator;

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

public class HelloApplication extends Application {
    static SongInformationService songService =new SongInformationService();
    static MusicBrainzAlbumService musicBrainzAlbumService =new MusicBrainzAlbumService();

    public static void main(String[] args) {
        launch();
    }
    Stage stage;
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

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

        ProgressBar progress = new ProgressBar(0);

        Button startButton=new Button();
        startButton.setText("Generate Playlist");
        startButton.setOnAction(e->generatePlaylist(directoryInput.getText(),progress,usernameInput.getText(),playlistInput.getText(),tagsCheckBox.isSelected()));

        Button closeButton=new Button();
        closeButton.setText("Close");
        closeButton.setOnAction(e->close());
        closeButton.setDisable(true);

        VBox pane=new VBox(15);
        pane.getChildren().addAll(directoryButton,directoryLabel,directoryInput,usernameLabel,usernameInput,playlistLabel,playlistInput,tagsCheckBox,progress,startButton,closeButton);
        pane.setAlignment(Pos.CENTER);

        Scene scene=new Scene(pane,500,300);

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
    public void generatePlaylist(String directory,ProgressBar progress, String userName,String playlistName,boolean manuallyConfigureMp3Tags){
        PlaylistGeneratingTask task = new PlaylistGeneratingTask(directory,userName,playlistName,manuallyConfigureMp3Tags);
//        progress.progressProperty().bind(task.progressProperty());
        task.setProgressBar(progress);
        new Thread(task).start();
//        Platform.runLater(task);
    }

    public void close(){
        stage.close();
    }

}