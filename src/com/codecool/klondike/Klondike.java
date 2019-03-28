package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URISyntaxException;

import javafx.event.EventHandler;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Media sound = setSound();
        MediaPlayer mediaPlayer = new MediaPlayer(sound);

        Button restart = new Button("Restart");
        setButtonDimensions(restart, 500, 850);

        Button moveCardsUp = new Button("Beam me up Scotty!");
        setButtonDimensions(moveCardsUp, 700, 850);

        Button silence = new Button("Shut the f*** up!");
        setButtonDimensions(silence, 570, 850);

        mediaPlayer.play();

        Card.loadCardImages();
        Game game = new Game();
        game.getChildren().add(restart);
        game.getChildren().add(moveCardsUp);
        game.getChildren().add(silence);
        game.setTableBackground(new Image("/table/pink.jpg"));

        moveCardsUp.setOnMouseClicked(game.buttonClickHandler);
        silence.setOnMouseClicked(mouseEvent -> mediaPlayer.stop());
        restart.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                mediaPlayer.stop();
                start(primaryStage);
            }
        });

        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

    }

    private Media setSound() {
        Media sound = null;
        try {
            sound = new Media(getClass().getResource("/music2.wav").toURI().toString());
        }catch(URISyntaxException e){
            e.printStackTrace();
        }
        return sound;
    }

    private void setButtonDimensions(Button button, int coordinateX, int coordinateY) {
        button.setLayoutX(coordinateX);
        button.setLayoutY(coordinateY);
    }

}
