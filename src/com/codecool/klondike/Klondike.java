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
        Media sound = null;
        try {
            sound = new Media(getClass().getResource("/music.wav").toURI().toString());
        }catch(URISyntaxException e){
            e.printStackTrace();
        }
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        Card.loadCardImages();
        Button restart = new Button("Restart");
        restart.setLayoutX(600);
        restart.setLayoutY(650);
        restart.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent mouseEvent){
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    mediaPlayer.stop();
                    start(primaryStage);
                    System.out.println("Yaay first step taken");
                }
            }
            });

        mediaPlayer.play();

        Game game = new Game();
        game.getChildren().add(restart);
        game.setTableBackground(new Image("/table/green.png"));


        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

    }

}
