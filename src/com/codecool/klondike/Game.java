package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 0;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        if (e.getClickCount() == 2) {
            Card card = (Card) e.getSource();
            if(card == card.getContainingPile().getTopCard()) {
                Card tableauTop = getValidIntersectingPile(card, tableauPiles).getTopCard();
                Pile.PileType cardPile = card.getContainingPile().getPileType();
                if (cardPile == Pile.PileType.TABLEAU || cardPile == Pile.PileType.DISCARD && card.equals(tableauTop)) {
                    int cardRank = card.getRank();
                    int cardSuite = card.getSuit();
                    if (cardRank == 1) {
                        findEmptyPile(card);
                    } else {
                        placeCardInFoundation(card, cardRank, cardSuite);
                    }
                    flipTableauTopCard();
                }
            } else {
                return;
            }
        }
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK && card.equals(stockPile.getTopCard())) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();

        if(activePile.getPileType() == Pile.PileType.TABLEAU) {
            List<Card> pileCards = activePile.getCards();
            int cardPosition = pileCards.indexOf(card);
            for (int i = 0; i < pileCards.size(); i++) {
                Card actualCard = pileCards.get(i);
                if (!actualCard.isFaceDown() && pileCards.indexOf(actualCard) >= cardPosition) {
                    setCardDimensions(offsetX, offsetY, actualCard);
                }
            }
        } else {
            setCardDimensions(offsetX, offsetY, card);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;

        Card card = (Card) e.getSource();
        Pile pile;
        double Y = e.getSceneY();

        if(Y < 250){
            pile = getValidIntersectingPile(card, foundationPiles);
        } else {
            pile = getValidIntersectingPile(card, tableauPiles);
        }

        if (isMoveValid(card, pile)){
            for(Card draggedCard : draggedCards){
                handleValidMove(draggedCard, pile);
            }
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }

        draggedCards.clear();
        flipTableauTopCard();
    };

    EventHandler<MouseEvent> buttonClickHandler = e -> autoPlaceFlippedCards();

    private void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    /* handling double click event */

    private void placeCardInFoundation(Card card, int cardRank, int cardSuite){
        for ( Pile pile: foundationPiles) {
            if (!pile.isEmpty()){
                int topCardRank = pile.getTopCard().getRank();
                int topCardSuite = pile.getTopCard().getSuit();
                if (cardRank - 1 == topCardRank && cardSuite == topCardSuite) {
                    handleValidMove(card, pile);
                }
            }
        }
    }

    private void findEmptyPile(Card card){
        for (Pile pile: foundationPiles){
            if(pile.isEmpty()){
                handleValidMove(card, pile);
                break;
            }
        }
    }

    private void refillStockFromDiscard() {

        if(stockPile.isEmpty()) {
            System.out.println("Stock refilled from discard pile.");
            List<Card> discardList = discardPile.getCards();
            Collections.reverse(discardList);
            for (Card card : discardList) {
                card.flip();
                stockPile.addCard(card);
            }
            discardPile.clear();
        }
    }

    /* auto-place cards */

    void autoPlaceFlippedCards() {
        int run = 7;
        List<Card> flippedCards = FXCollections.observableArrayList();

        if(discardPile.getTopCard() != null && !discardPile.getTopCard().isFaceDown()) {
            flippedCards.add(discardPile.getTopCard());
        }

        while(run>0){
            for (Pile pile : tableauPiles) {
                for (Card card : pile.getCards()){
                    if (!card.isFaceDown()) {
                        flippedCards.add(pile.getTopCard());
                    }
                }
            }

            for (Card card : flippedCards) {
                int cardRank = card.getRank();
                int cardSuite = card.getSuit();
                if (cardRank == 1) {
                    findEmptyPile(card);
                } else {
                    placeCardInFoundation(card, cardRank, cardSuite);
                }

            }
            run--;
        }
        flipTableauTopCard();
    }

    /* handling card dragging */

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private boolean isMoveValid(Card card, Pile destPile) {
        if(destPile.getPileType() == Pile.PileType.TABLEAU && destPile.isEmpty()){
            return card.getRank() == 13;
        }
        if(destPile.getPileType() == Pile.PileType.TABLEAU && !destPile.isEmpty()) {
            String cardColor = Card.getCardColor(card);
            String targetCardColor = Card.getCardColor(destPile.getTopCard());
            return !cardColor.equals(targetCardColor) && card.getRank() + 1 == destPile.getTopCard().getRank();
        }
        else if(destPile.getPileType() == Pile.PileType.FOUNDATION && draggedCards.size() == 1) { // thanks
            if (destPile.isEmpty()){
                return card.getRank() == 1;
            }
            else {
                int cardSuit = card.getSuit();
                int targetCardSuit = destPile.getTopCard().getSuit();
                return cardSuit == targetCardSuit && card.getRank() - 1 == destPile.getTopCard().getRank();
            }
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = card.getContainingPile();
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        card.moveToPile(destPile);
        if(isGameWon()){
            showPopUp();
        }
        //MouseUtil.slideToDest(draggedCards, destPile);
    }

    private void flipTableauTopCard() {
        for (Pile piles : tableauPiles) {
            if (!piles.isEmpty()) {
                Card topCard = piles.getTopCard();
                if (topCard.isFaceDown()) {
                    topCard.flip();
                }
            }
        }
    }

    /* checking for victory */

    private boolean isGameWon() {
        for (Pile foundationPile : foundationPiles) {
            int size = foundationPile.getSize();
            if (size != 13) return false;
        }
        return true;
    }

    private void showPopUp() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Congratulations! You won the game!"));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /* initializing game elements such as: piles, cards and background */

    Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        setPileLocation(stockPile, 95, 20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        setPileLocation(discardPile, 285, 20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            setPileLocation(foundationPile, 610 + i * 180, 20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            setPileLocation(tableauPile, 95 + i * 180, 275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    private void setPileLocation(Pile stockPile, int i2, int i3) {
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(i2);
        stockPile.setLayoutY(i3);
    }

    private void setCardDimensions(double offsetX, double offsetY, Card actualCard) {
        draggedCards.add(actualCard);
        actualCard.getDropShadow().setRadius(20);
        actualCard.getDropShadow().setOffsetX(10);
        actualCard.getDropShadow().setOffsetY(10);
        actualCard.toFront();
        actualCard.setTranslateX(offsetX);
        actualCard.setTranslateY(offsetY);
    }

    private void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

        List<Card> shuffledDeck = stockPile.getCards();
        int index = shuffledDeck.size() - 1;
        for (int i=0; i<tableauPiles.size(); i++) {
            for(int j=0; j<i+1; j++) {
                shuffledDeck.get(index).moveToPile(tableauPiles.get(i));
                index--;
            }
            tableauPiles.get(i).getTopCard().flip();
        }
    }

    void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
