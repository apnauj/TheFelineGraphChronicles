package com.eia.feline.ui.viz;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.IntConsumer;

/**
 * Barra de reproduccion compartida por las cuatro misiones: play, pausa, paso a
 * paso, reinicio y velocidad.
 *
 * No sabe QUE se esta animando. Solo cuenta pasos y, en cada uno, le avisa al
 * visualizador con un entero. Eso deja toda la logica de dibujo del lado del
 * visualizador y permite que la misma barra sirva para una cuadricula, un grafo
 * o una cola de cables ordenados.
 */
public final class Playback {

    private static final double BASE_STEP_MILLIS = 90;

    private final VBox root = new VBox(8);
    private final Button playPause = new Button("Reproducir");
    private final Button stepBack = new Button("<");
    private final Button stepForward = new Button(">");
    private final Button reset = new Button("Reiniciar");
    private final Slider scrubber = new Slider(0, 0, 0);
    private final Slider speed = new Slider(0.25, 8, 1);
    private final Label counter = new Label("0 / 0");
    private final Label speedLabel = new Label("1.0x");

    private final SimpleIntegerProperty step = new SimpleIntegerProperty(0);

    private IntConsumer sink;
    private int totalSteps;
    private Timeline timeline;

    public Playback() {
        for (Button b : new Button[]{ stepBack, stepForward }) {
            b.getStyleClass().addAll("button-ghost", "icon-button");
        }
        playPause.getStyleClass().add("button-primary");
        reset.getStyleClass().add("button-ghost");

        counter.getStyleClass().addAll("caption", "mono");
        speedLabel.getStyleClass().addAll("caption", "mono");

        scrubber.setBlockIncrement(1);
        HBox.setHgrow(scrubber, Priority.ALWAYS);

        speed.setPrefWidth(140);
        speed.valueProperty().addListener((o, was, now) -> {
            speedLabel.setText(String.format("%.2fx", now.doubleValue()));
            if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
                // Reconstruir es mas simple y mas fiable que cambiarle la tasa a una
                // Timeline en marcha, y el salto no se nota a estas velocidades.
                boolean wasRunning = true;
                stopTimeline();
                if (wasRunning) startTimeline();
            }
        });

        scrubber.valueProperty().addListener((o, was, now) -> {
            if (scrubber.isValueChanging() || !scrubber.isFocused()) {
                setStep((int) Math.round(now.doubleValue()));
            }
        });

        step.addListener((o, was, now) -> {
            counter.setText(now.intValue() + " / " + totalSteps);
            if (sink != null) sink.accept(now.intValue());
        });

        playPause.setOnAction(e -> toggle());
        stepBack.setOnAction(e -> { pause(); setStep(step.get() - 1); });
        stepForward.setOnAction(e -> { pause(); setStep(step.get() + 1); });
        reset.setOnAction(e -> { pause(); setStep(0); });

        Label speedTag = new Label("Velocidad");
        speedTag.getStyleClass().add("caption");

        HBox controls = new HBox(8, playPause, stepBack, stepForward, reset,
                spacer(), speedTag, speed, speedLabel);
        controls.setAlignment(Pos.CENTER_LEFT);

        HBox track = new HBox(12, scrubber, counter);
        track.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(track, controls);
        root.setPadding(new Insets(0));
        detach();
    }

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public VBox node() { return root; }

    /** Conecta un visualizador y le dice cuantos pasos tiene la animacion. */
    public void attach(IntConsumer sink, int totalSteps) {
        stopTimeline();
        this.sink = sink;
        this.totalSteps = Math.max(0, totalSteps);

        scrubber.setMax(this.totalSteps);
        boolean animatable = this.totalSteps > 0;
        setControlsEnabled(animatable);

        setStep(0);
        counter.setText("0 / " + this.totalSteps);
    }

    /** Deja la barra inerte: no hay nada que reproducir. */
    public void detach() {
        stopTimeline();
        sink = null;
        totalSteps = 0;
        scrubber.setMax(0);
        scrubber.setValue(0);
        counter.setText("0 / 0");
        setControlsEnabled(false);
    }

    public void stop() {
        stopTimeline();
        playPause.setText("Reproducir");
    }

    private void setControlsEnabled(boolean enabled) {
        playPause.setDisable(!enabled);
        stepBack.setDisable(!enabled);
        stepForward.setDisable(!enabled);
        reset.setDisable(!enabled);
        scrubber.setDisable(!enabled);
        speed.setDisable(!enabled);
    }

    private void toggle() {
        if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
            pause();
        } else {
            if (step.get() >= totalSteps) setStep(0);   // reproducir desde el final reinicia
            startTimeline();
        }
    }

    private void pause() {
        stopTimeline();
        playPause.setText("Reproducir");
    }

    private void startTimeline() {
        if (totalSteps <= 0) return;
        timeline = new Timeline(new KeyFrame(
                Duration.millis(BASE_STEP_MILLIS / speed.getValue()),
                e -> {
                    if (step.get() >= totalSteps) {
                        pause();
                    } else {
                        setStep(step.get() + 1);
                    }
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        playPause.setText("Pausa");
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void setStep(int value) {
        int clamped = Math.max(0, Math.min(totalSteps, value));
        if (clamped != step.get()) step.set(clamped);
        if (Math.round(scrubber.getValue()) != clamped) scrubber.setValue(clamped);
    }
}
