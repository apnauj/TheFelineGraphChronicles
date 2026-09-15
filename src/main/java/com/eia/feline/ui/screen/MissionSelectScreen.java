package com.eia.feline.ui.screen;

import com.eia.feline.ui.fx.Ink;
import com.eia.feline.ui.theme.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
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

    /** Caja donde se encaja el retrato, en pixeles. Fija a proposito: ver card(). */
    private static final double PORTRAIT_W = 212;
    private static final double PORTRAIT_H = 340;

    public MissionSelectScreen(Navigator navigator) {
        setPadding(new Insets(30, 40, 26, 40));
        // Cada pantalla pinta su propio papel tramado en vez de fiarse del
        // contenedor. Va como Background y no como un nodo: ver Ink.paperBackground.
        Ink.paperBackground(this, Theme.PAPER, Theme.CAPE, 0.20);

        Button back = new Button("< PORTADA");
        back.getStyleClass().add("button-ghost");
        back.setOnAction(e -> {
            LoadingScreen cover = LoadingScreen.cover(
                    () -> navigator.go(new MissionSelectScreen(navigator)));
            navigator.go(cover);
            cover.play();
        });

        Label title = new Label("ELIGE TU MISION");
        title.getStyleClass().add("display");

        Label blurb = new Label(
                "Limon y Nero se robaron las cuentas de Claude y secuestraron a Nina. "
                        + "Cada pista que dejaron es un grafo.");
        blurb.getStyleClass().add("subtitle");
        blurb.setWrapText(true);
        blurb.setMaxWidth(760);

        HBox titleRow = new HBox(16, back, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(8, titleRow, blurb);
        header.setPadding(new Insets(0, 0, 22, 0));

        // HBox y no FlowPane: con setFillHeight(true) el HBox estira las vinetas a
        // la altura de la fila por si mismo. Atar la altura de la vineta a la del
        // contenedor (o a la de la pantalla) crea un ciclo de medicion -- el alto
        // del padre depende del alto preferido del hijo, que depende del alto del
        // padre -- y el resultado fue una pantalla de 978 px dentro de una escena
        // de 820, con el contenido desplazado fuera de la ventana.
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.TOP_LEFT);
        cards.setFillHeight(true);

        for (MissionDescriptor<?> mission : Missions.all()) {
            Node card = card(mission, navigator);
            HBox.setHgrow(card, Priority.ALWAYS);
            cards.getChildren().add(card);
        }

        VBox body = new VBox(header, cards);
        VBox.setVgrow(cards, Priority.ALWAYS);

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

        Label badge = new Label(mission.implemented() ? "LISTA" : "ANDAMIAJE");
        badge.getStyleClass().addAll("badge", mission.implemented() ? "badge-ready" : "badge-pending");

        Node portrait = mission.portrait().get();
        StackPane portraitHolder = new StackPane(portrait);
        portraitHolder.setMinHeight(140);

        // El retrato se encaja en una caja FIJA, por ancho y por alto.
        //
        // Dos razones, y las dos se pagaron caras:
        //
        // 1. Atando solo fitHeight, con preserveRatio el ancho sale de la
        //    proporcion de la imagen y se sale de la vineta: los gatos son de 0,74
        //    y a 400 px de alto piden 297 de ancho, mas de los 232 que hay. Con
        //    churun, que es apaisado (4,6), el desbordamiento es enorme.
        // 2. Atar fitWidth al ancho del contenedor arregla eso y crea un ciclo de
        //    medicion: ImageView no es redimensionable, asi que sus limites entran
        //    en el ancho preferido del padre, que agranda al padre, que agranda a
        //    la imagen. La pantalla acabo midiendo 1579 px de ancho en una ventana
        //    de 1280.
        //
        // Con valores fijos no hay realimentacion posible. La imagen no crece con
        // la ventana, que es un precio pequeno por un layout que no se descuadra.
        if (portrait instanceof ImageView art) {
            art.setPreserveRatio(true);
            art.setFitWidth(PORTRAIT_W);
            art.setFitHeight(PORTRAIT_H);
        }
        // Red de seguridad: nada puede pintarse fuera de la vineta.
        Ink.clipToBounds(portraitHolder);
        // El retrato se queda con el espacio sobrante y el texto baja al pie de la
        // vineta, como el cartucho de narracion de un comic.
        VBox.setVgrow(portraitHolder, Priority.ALWAYS);

        VBox text = new VBox(6, number, name, tagline, algorithms);
        VBox content = new VBox(12, portraitHolder, text, badge);
        content.setAlignment(Pos.TOP_LEFT);
        content.setFillWidth(true);

        VBox card = new VBox(content);
        VBox.setVgrow(content, Priority.ALWAYS);
        card.getStyleClass().add("mission-card");
        card.setPrefWidth(272);
        card.setMinWidth(210);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(320);
        // maxHeight sin limite es lo que permite que el HBox la estire; con el
        // valor por defecto se quedaria en su altura preferida.
        card.setMaxHeight(Double.MAX_VALUE);

        // Un grado y medio de giro, alternando el sentido: basta para que la pagina
        // deje de parecer una rejilla de software y parezca una plancha dibujada.
        card.setRotate((mission.number() % 2 == 0) ? 1.4 : -1.4);

        DropShadow misprint = Ink.misprint(Theme.fade(Theme.INK, 0.55), 6, 6);
        card.setEffect(misprint);

        // La vineta se rasteriza una vez y se reutiliza.
        //
        // Antes el retrato hacia un rebote perpetuo. Mover un hijo dentro de una
        // vineta que lleva sombra obliga a recalcular la sombra de TODA la vineta
        // en cada fotograma, y son cuatro vinetas a la vez, todo el rato, sin que
        // nadie las este mirando. Se quita el rebote y se cachea: el menu deja de
        // costar nada mientras se decide que mision abrir.
        card.setCache(true);
        card.setCacheHint(CacheHint.SPEED);

        double restAngle = card.getRotate();
        card.setOnMouseEntered(e -> hover(card, restAngle, true));
        card.setOnMouseExited(e -> hover(card, restAngle, false));
        card.setOnMouseClicked(e -> navigator.go(new MissionScreen<>(mission, navigator)));

        return card;
    }

    /**
     * Al pasar por encima la vineta se endereza y se levanta. No hay transiciones
     * en el CSS de JavaFX, asi que el movimiento se hace aqui con un Timeline.
     */
    /**
     * Al pasar por encima la vineta se endereza y se levanta.
     *
     * Solo se animan escala, giro y desplazamiento: son transformaciones, y sobre
     * un nodo cacheado la tarjeta grafica las aplica al mapa de bits ya
     * rasterizado. Antes tambien se animaba el desplazamiento de la SOMBRA, y eso
     * obliga a recalcular el efecto -- y a tirar la cache -- en cada fotograma.
     */
    private void hover(Region card, double restAngle, boolean entering) {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(150),
                new KeyValue(card.scaleXProperty(), entering ? 1.045 : 1.0, Interpolator.EASE_OUT),
                new KeyValue(card.scaleYProperty(), entering ? 1.045 : 1.0, Interpolator.EASE_OUT),
                new KeyValue(card.rotateProperty(), entering ? 0 : restAngle, Interpolator.EASE_OUT),
                new KeyValue(card.translateYProperty(), entering ? -8 : 0, Interpolator.EASE_OUT)));
        t.play();
    }

    private Node footer() {
        Label hint = new Label("Cada mision trae cargado el ejemplo del enunciado.");
        hint.getStyleClass().add("caption");

        Label credit = new Label("Lenguajes y Compiladores - Universidad EIA");
        credit.getStyleClass().add("caption");
        credit.setTextFill(Theme.INK_SOFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(hint, spacer, credit);
        bar.setPadding(new Insets(22, 0, 0, 0));
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }
}
