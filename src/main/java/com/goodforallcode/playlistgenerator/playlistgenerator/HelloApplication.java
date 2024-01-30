package com.goodforallcode.playlistgenerator.playlistgenerator;

import com.goodforallcode.playlistgenerator.playlistgenerator.model.Mp3Info;
import com.goodforallcode.playlistgenerator.playlistgenerator.service.SongInformationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HelloApplication extends Application {
    static SongInformationService service=new SongInformationService();

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
        directoryButton.setText("Choose Directory");
        directoryButton.setOnAction(e->chooseDirectory(primaryStage,directoryInput));

        Label directoryLabel=new Label();
        directoryLabel.setText("Directory");
        directoryLabel.setLabelFor(directoryInput);

        ProgressBar progress = new ProgressBar(0);

        Button startButton=new Button();
        startButton.setText("Generate Playlist");
        startButton.setOnAction(e->generatePlaylist(directoryInput,progress));

        Button closeButton=new Button();
        closeButton.setText("Close");
        closeButton.setOnAction(e->close());
        closeButton.setDisable(true);

        VBox pane=new VBox(15);
        pane.getChildren().addAll(directoryButton,directoryLabel,directoryInput,progress,startButton,closeButton);
        pane.setAlignment(Pos.CENTER);

        Scene scene=new Scene(pane,300,300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Generate Playlist");
        primaryStage.setOnCloseRequest(e->{e.consume();close();});
        primaryStage.show();
    }
    public void chooseDirectory(Stage stage,TextField directoryInput){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("JavaFX Projects");

        File defaultDirectory = new File("c:/");
        chooser.setInitialDirectory(defaultDirectory);

        File selectedDirectory = chooser.showDialog(stage);
        directoryInput.setText(selectedDirectory.getAbsolutePath());
    }
    public void generatePlaylist(TextField directoryInput,ProgressBar progress){
        Set<String> visitedDirectories=new HashSet();
        Set<String> remainingDirectories=new HashSet();
        Set<Mp3Info> songs = null;
        try {
            songs = service.getSongInformation(directoryInput.getText(),visitedDirectories,remainingDirectories,progress);
            for(Mp3Info entry:songs){
                System.out.println(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void close(){
        stage.close();
    }

}