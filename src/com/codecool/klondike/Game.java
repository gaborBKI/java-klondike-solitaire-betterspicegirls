package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

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
            System.out.println("double clicked");
            Card card = (Card) e.getSource();
            if (card.getContainingPile().getPileType() == Pile.PileType.TABLEAU && card.equals(getValidIntersectingPile(card, tableauPiles).getTopCard())) {
                int cardRank = card.getRank();
                int cardSuite = card.getSuit();
                if (cardRank == 1) {
                    findEmptyPile(card);
                } else {
                    placeCardInFoundation(card, cardRank, cardSuite);
                }
                for(Pile piles : tableauPiles){
                    if(!piles.isEmpty()) {
                        Card topCard = piles.getTopCard();
                        System.out.println(topCard);
                        if (topCard.isFaceDown()) {
                            topCard.flip();
                        }
                    }
                }
            }
        }
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK && card.equals(stockPile.getTopCard())) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    public void placeCardInFoundation(Card card, int cardRank, int cardSuite){
        for ( Pile pile: foundationPiles) {
            if (!pile.isEmpty()){
                int topCardRank = pile.getTopCard().getRank();
                int topCardSuite = pile.getTopCard().getSuit();
                if (cardRank - 1 == topCardRank && cardSuite == topCardSuite) {
                    //pile.addCard(card);
                    handleValidMove(card, pile);
                    card.moveToPile(pile);
                    System.out.println("The Magic happened");
                }
            }
        }
    }

    public void findEmptyPile(Card card){
        for (Pile pile: foundationPiles){
            if(pile.isEmpty()){
                card.moveToPile(pile);
                handleValidMove(card, pile);
                break;
            }
        }
    }

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
                    draggedCards.add(actualCard);
                    actualCard.getDropShadow().setRadius(20);
                    actualCard.getDropShadow().setOffsetX(10);
                    actualCard.getDropShadow().setOffsetY(10);

                    actualCard.toFront();

                    actualCard.setTranslateX(offsetX);
                    actualCard.setTranslateY(offsetY);
                }
            }
        } else {
            draggedCards.add(card);
            card.getDropShadow().setRadius(20);
            card.getDropShadow().setOffsetX(10);
            card.getDropShadow().setOffsetY(10);

            card.toFront();

            card.setTranslateX(offsetX);
            card.setTranslateY(offsetY);
        }
        //draggedCards.add(card);

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
        //TODO
        if (isMoveValid(card, pile)){
            for(Card draggedCard : draggedCards){
                handleValidMove(draggedCard, pile);
                draggedCard.moveToPile(pile);
            }

            // ÖSSZECSÚSZIK!

            //MouseUtil.slideToDest(draggedCards, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
        draggedCards.clear();

        for(Pile piles : tableauPiles){
            if(!piles.isEmpty()) {
                Card topCard = piles.getTopCard();
                if (topCard.isFaceDown()) {
                    topCard.flip();
                }
            }
        }
    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void onDoubleMouseClicked(MouseEvent event)
    {
        if (event.getClickCount() == 2) {
            System.out.println("double clicked");
        }
    }

    public void refillStockFromDiscard() {
        //TODO
        if(stockPile.isEmpty()) {
            System.out.println("Stock refilled from discard pile.");
            // Tomi added:
            List<Card> discardList = discardPile.getCards();
            //TODO
            Collections.reverse(discardList);
            for (Card card : discardList) {
                card.flip();
                stockPile.addCard(card);
            }
            discardPile.clear();
        }
        /*
        discardIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            // addMouseEventHandlers(card);
            // getChildren().add(card);
        });
        */

    }

    public boolean isMoveValid(Card card, Pile destPile) {
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
                if(card.getRank() ==1){
                    return true;
                }
                else {
                    return false;
                }
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

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
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
        System.out.println(msg);
        //MouseUtil.slideToDest(draggedCards, destPile);
        //card.moveToPile(destPile);
        //draggedCards.clear();

        // flip the last card on the tableau if it is face down after placing a card

        /*for(Pile pile : tableauPiles){
            if(!pile.isEmpty()) {
                Card topCard = pile.getTopCard();
                if (topCard.isFaceDown()) {
                    topCard.flip();
                }
            }
        }*/
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        //TODO
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

        // Klondike setup solution, needs some rework, as the code is possibly not very nice.

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

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
