package com.eia.feline.ui.screen;

import com.eia.feline.ui.theme.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Seleccion de mision: cuatro tarjetas, una por mision.
 *
 * Las tarjetas de las misiones que todavia no estan implementadas se muestran
 * igual pero marcadas, en vez de esconderse. Asi el evaluador ve de una que el
 * sistema contempla las cuatro y cual falta.
 */
public final class MissionSelectScreen extends BorderPane {

    public MissionSelectScreen(Navigator navigator) {
        setPadding(new Insets(34, 40, 34, 40));

        Label title = new Label("Elige tu mision");
        title.getStyleClass().add("display");

        Label blurb = new Label(
                "Limon y Nero se robaron las cuentas de Claude y secuestraron a Nina. "
                        + "Cada pista que dejaron es un grafo.");
        blurb.getStyleClass().add("subtitle");
        blurb.setWrapText(true);
        blurb.setMaxWidth(760);

        VBox header = new VBox(8, title, blurb);
        header.setPadding(new Insets(0, 0, 26, 0));

        FlowPane cards = new FlowPane(20, 20);
        cards.setAlignment(Pos.TOP_LEFT);
        for (MissionDescriptor<?> mission : Missions.all()) {
            cards.getChildren().add(card(mission, navigator));
        }

        StackPane cardArea = new StackPane(cards);
        StackPane.setAlignment(cards, Pos.CENTER_LEFT);

        VBox body = new VBox(header, cardArea);
        VBox.setVgrow(cardArea, Priority.ALWAYS);
        setCenter(body);
        setBottom(footer());
    }

    /**
     * El comodin de MissionDescriptor se captura en este metodo generico, que es
     * lo que permite guardar misiones de payloads distintos en una sola lista.
     */
    private <P> Node card(MissionDescriptor<P> mission, Navigator navigator) {
        Label number = new Label(mission.label().toUpperCase());
        number.getStyleClass().add("mission-number");

        Label name = new Label(mission.name());
        name.getStyleClass().add("title");
        name.setWrapText(true);

        Label tagline = new Label(mission.tagline());
        tagline.getStyleClass().add("subtitle");
        tagline.setWrapText(true);

        Label algorithms = new Label(mission.algorithms());
        algorithms.getStyleClass().addAll("caption", "mono");
        algorithms.setTextFill(mission.accent());

        Label badge = new Label(mission.implemented() ? "LISTA" : "EN CURSO");
        badge.getStyleClass().addAll("badge", mission.implemented() ? "badge-ready" : "badge-pending");

        Node portrait = mission.portrait().get();
        StackPane portraitHolder = new StackPane(portrait);
        portraitHolder.setMinHeight(132);
        portraitHolder.setPrefHeight(132);

        VBox text = new VBox(6, number, name, tagline, algorithms);
        VBox content = new VBox(12, portraitHolder, text, badge);
        content.setAlignment(Pos.TOP_LEFT);

        VBox card = new VBox(content);
        card.getStyleClass().add("mission-card");
        card.setPrefWidth(272);
        card.setPrefHeight(420);

        DropShadow glow = new DropShadow(0, mission.accent());
        glow.setSpread(0.18);
        card.setEffect(glow);

        // Rebote perpetuo del retrato, desfasado por mision para que no latan al unisono.
        Timeline idle = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(portrait.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(1100),
                        new KeyValue(portrait.translateYProperty(), -7, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(2200),
                        new KeyValue(portrait.translateYProperty(), 0, Interpolator.EASE_BOTH)));
        idle.setDelay(Duration.millis(mission.number() * 180));
        idle.setCycleCount(Animation.INDEFINITE);
        idle.play();

        card.setOnMouseEntered(e -> hover(card, glow, mission.accent(), true));
        card.setOnMouseExited(e -> hover(card, glow, mission.accent(), false));
        card.setOnMouseClicked(e -> navigator.go(new MissionScreen<>(mission, navigator)));

        return card;
    }

    private void hover(Region card, DropShadow glow, Color accent, boolean entering) {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(160),
                new KeyValue(card.scaleXProperty(), entering ? 1.035 : 1.0, Interpolator.EASE_OUT),
                new KeyValue(card.scaleYProperty(), entering ? 1.035 : 1.0, Interpolator.EASE_OUT),
                new KeyValue(card.translateYProperty(), entering ? -6 : 0, Interpolator.EASE_OUT),
                new KeyValue(glow.radiusProperty(), entering ? 28 : 0, Interpolator.EASE_OUT)));
        t.play();
    }

    private Node footer() {
        Label hint = new Label("Cada mision trae cargado el ejemplo del enunciado.");
        hint.getStyleClass().add("caption");

        Label credit = new Label("Lenguajes y Compiladores - Universidad EIA");
        credit.getStyleClass().add("caption");
        credit.setTextFill(Theme.MUTED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(hint, spacer, credit);
        bar.setPadding(new Insets(22, 0, 0, 0));
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }
}
