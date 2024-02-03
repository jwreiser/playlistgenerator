package com.goodforallcode.playlistgenerator.javafx;

import com.goodforallcode.playlistgenerator.playlistgenerator.util.Mp3FileUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class TooManyOptions {
    Path directory;
    String optionName="";
    List<String> options;

    String choice=null;
    public TooManyOptions(Path directory, String optionName, List<String> options) {
        this.directory = directory;
        this.optionName = optionName;
        this.options = options;
    }

    public boolean show(){
        Stage stage=new Stage();

        TextField directoryInput=new TextField();
        if(directory!=null) {
            directoryInput.appendText(directory.toAbsolutePath().toString());
        }
        directoryInput.setEditable(false);

        Label directoryLabel=new Label();
        directoryLabel.setText("Directory");
        directoryLabel.setLabelFor(directoryInput);

        //don't just modify passed in options as they may not be modifiable
        List<String>updatedOptions=new ArrayList<>();
        updatedOptions.addAll(options);
        if(directory!=null) {
            updatedOptions.add(directory.getFileName().toString());
            updatedOptions.add(directory.getParent().getFileName().toString());
        }
        final ComboBox optionsChoiceBox = new ComboBox();
        optionsChoiceBox.getItems().addAll(updatedOptions);

        Label optionsLabel=new Label();
        optionsLabel.setText(optionName);
        optionsLabel.setLabelFor(optionsChoiceBox);

        TextField manuaInput=new TextField();
        Label manuaLabel=new Label();
        manuaLabel.setText("If you dont see a good option enter one");
        manuaLabel.setLabelFor(manuaInput);


        Button saveButton=new Button("Save Choice");
        saveButton.setOnAction(e-> {choice=getChoice(optionsChoiceBox,manuaInput);stage.close();});
        VBox pane=new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getChildren().addAll(directoryLabel,directoryInput,optionsLabel,optionsChoiceBox,manuaLabel,manuaInput,saveButton);

        Scene scene=new Scene(pane,800,400);
        stage.setScene(scene);

        stage.setTitle("Please disambiguate "+optionName+"s");
        stage.showAndWait();
        return true;
    }

    private String getChoice(ComboBox optionsChoiceBox,TextField manuaInput){
        if(!manuaInput.getText().isEmpty()){
            return manuaInput.getText();
        }else{
            return optionsChoiceBox.getValue().toString();
        }
    }
}
