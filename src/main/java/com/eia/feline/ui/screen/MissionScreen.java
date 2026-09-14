package com.eia.feline.ui.screen;

import com.eia.feline.missions.CaseResult;
import com.eia.feline.missions.InputFormatException;
import com.eia.feline.missions.MissionSolver;
import com.eia.feline.ui.theme.Theme;
import com.eia.feline.ui.viz.Playback;
import com.eia.feline.ui.viz.Visualizer;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * La pantalla de una mision. Es UNA sola clase para las cuatro: lo que cambia
 * viene en el MissionDescriptor.
 *
 * Disposicion: entrada a la izquierda, dibujo en el centro, salida a la derecha
 * y barra de reproduccion abajo.
 *
 * El calculo corre en un Task y no en el hilo de JavaFX. Con la cuadricula del
 * limite (10^6 celdas) resolver toma del orden de un segundo, y hacerlo en el
 * hilo grafico congelaria la ventana entera.
 *
 * @param <P> payload de visualizacion de la mision.
 */
public final class MissionScreen<P> extends BorderPane {

    private final MissionDescriptor<P> mission;

    private final TextArea input = new TextArea();
    private final TextArea output = new TextArea();
    private final Label status = new Label();
    private final VBox banner = new VBox();
    private final StackPane canvasHolder = new StackPane();
    private final ComboBox<String> caseChooser = new ComboBox<>();
    private final ProgressIndicator spinner = new ProgressIndicator();
    private final Button solveButton = new Button("Resolver");

    private final Visualizer<P> visualizer;
    private final Playback playback = new Playback();

    private List<CaseResult<P>> results = List.of();

    public MissionScreen(MissionDescriptor<P> mission, Navigator navigator) {
        this.mission = mission;
        this.visualizer = (mission.visualizer() == null) ? null : mission.visualizer().get();

        setPadding(new Insets(20, 26, 20, 26));
        setTop(header(navigator));
        setCenter(body());
        setBottom(playbackBar());

        if (mission.solver() != null) {
            input.setText(mission.solver().sampleInput());
        } else {
            input.setPromptText("Mision pendiente");
            input.setDisable(true);
        }
        showIdleCanvas();

        // Honestidad sobre el estado real: estas misiones ya funcionan, pero sobre
        // algoritmos de andamiaje escritos para poder construir la interfaz. No se
        // puede dejar que parezcan terminadas.
        if (!mission.implemented() && mission.solver() != null) {
            showBanner("banner-info", "Andamiaje temporal",
                    "Los algoritmos de esta mision son provisionales, escritos para poder"
                            + " construir y demostrar la interfaz. La implementacion definitiva"
                            + " la esta escribiendo otro integrante; cuando llegue, se cambian"
                            + " las llamadas del solver y ni esta pantalla ni el dibujo cambian.");
        }
    }

    // ---------------------------------------------------------------- cabecera

    private Node header(Navigator navigator) {
        Button back = new Button("< Volver");
        back.getStyleClass().add("button-ghost");
        back.setOnAction(e -> {
            playback.stop();
            navigator.go(new MissionSelectScreen(navigator));
        });

        Label title = new Label(mission.label() + " - " + mission.name());
        title.getStyleClass().add("title");

        Label algorithms = new Label(mission.algorithms());
        algorithms.getStyleClass().addAll("caption", "mono");
        algorithms.setTextFill(mission.accent());

        VBox titles = new VBox(2, title, algorithms);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        spinner.setVisible(false);
        spinner.setPrefSize(22, 22);

        status.getStyleClass().add("caption");

        HBox bar = new HBox(16, back, titles, spacer, spinner, status);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 16, 0));
        return bar;
    }

    // ------------------------------------------------------------------ cuerpo

    private Node body() {
        HBox columns = new HBox(16, inputPanel(), canvasPanel(), outputPanel());
        HBox.setHgrow(columns.getChildren().get(1), Priority.ALWAYS);
        return columns;
    }

    private Node inputPanel() {
        Label label = new Label("ENTRADA");
        label.getStyleClass().add("section-label");

        input.setPrefColumnCount(26);
        VBox.setVgrow(input, Priority.ALWAYS);

        Button loadSample = new Button("Cargar ejemplo");
        loadSample.getStyleClass().add("button-ghost");
        loadSample.setDisable(mission.solver() == null);
        loadSample.setOnAction(e -> {
            input.setText(mission.solver().sampleInput());
            clearBanner();
        });

        solveButton.getStyleClass().add("button-primary");
        solveButton.setDisable(mission.solver() == null);
        solveButton.setOnAction(e -> solve());

        HBox actions = new HBox(8, loadSample, solveButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, label, input, actions);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(340);
        panel.setMinWidth(280);
        return panel;
    }

    private Node canvasPanel() {
        Label label = new Label("VISUALIZACION");
        label.getStyleClass().add("section-label");

        canvasHolder.getStyleClass().add("panel-sunken");
        VBox.setVgrow(canvasHolder, Priority.ALWAYS);

        banner.setSpacing(6);
        banner.setVisible(false);
        banner.setManaged(false);

        VBox panel = new VBox(10, label, banner, canvasHolder);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(16));
        panel.setMinWidth(320);
        return panel;
    }

    private Node outputPanel() {
        Label label = new Label("SALIDA");
        label.getStyleClass().add("section-label");

        Label note = new Label("Texto exacto, comparable caracter por caracter.");
        note.getStyleClass().add("caption");

        output.setEditable(false);
        output.setPrefColumnCount(24);
        VBox.setVgrow(output, Priority.ALWAYS);

        Label caseLabel = new Label("Caso a dibujar");
        caseLabel.getStyleClass().add("caption");

        caseChooser.setMaxWidth(Double.MAX_VALUE);
        caseChooser.setDisable(true);
        caseChooser.setOnAction(e -> {
            int i = caseChooser.getSelectionModel().getSelectedIndex();
            if (i >= 0 && i < results.size()) draw(results.get(i).payload());
        });

        VBox panel = new VBox(10, label, note, output, caseLabel, caseChooser);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(320);
        panel.setMinWidth(260);
        return panel;
    }

    private Node playbackBar() {
        VBox bar = playback.node();
        bar.setPadding(new Insets(16, 0, 0, 0));
        return bar;
    }

    // --------------------------------------------------------------- resolver

    private void solve() {
        MissionSolver<P> solver = mission.solver();
        if (solver == null) return;

        clearBanner();
        playback.stop();
        String raw = input.getText();

        Task<List<CaseResult<P>>> task = new Task<>() {
            @Override
            protected List<CaseResult<P>> call() throws InputFormatException {
                return solver.solve(raw);
            }
        };

        task.setOnRunning(e -> {
            spinner.setVisible(true);
            solveButton.setDisable(true);
            status.setText("Resolviendo...");
        });

        task.setOnSucceeded(e -> {
            spinner.setVisible(false);
            solveButton.setDisable(false);
            results = task.getValue();
            output.setText(MissionSolver.render(results));
            status.setText(results.size() + (results.size() == 1 ? " caso resuelto" : " casos resueltos"));

            caseChooser.getItems().setAll(
                    results.stream().map(r -> "Caso #" + r.index()).toList());
            caseChooser.setDisable(results.isEmpty());
            if (!results.isEmpty()) {
                caseChooser.getSelectionModel().select(0);
                draw(results.get(0).payload());
            } else {
                showIdleCanvas();
            }
        });

        task.setOnFailed(e -> {
            spinner.setVisible(false);
            solveButton.setDisable(false);
            output.clear();
            status.setText("");
            caseChooser.getItems().clear();
            caseChooser.setDisable(true);
            showIdleCanvas();

            Throwable cause = task.getException();
            // La entrada mal formada es un caso previsto y se explica; cualquier otra
            // cosa es un error nuestro, y tambien se muestra en la ventana, nunca en
            // la consola como traza.
            if (cause instanceof InputFormatException) {
                showBanner("banner-error", "Entrada invalida", cause.getMessage());
            } else {
                showBanner("banner-error", "No se pudo resolver",
                        cause == null ? "Error desconocido"
                                : cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
        });

        Thread worker = new Thread(task, "solver-mision-" + mission.number());
        worker.setDaemon(true);
        worker.start();
    }

    // --------------------------------------------------------------- dibujar

    private void draw(P payload) {
        playback.stop();
        clearBanner();

        if (visualizer == null) {
            showPendingCanvas();
            return;
        }
        if (!visualizer.withinDrawBudget(payload)) {
            // La seccion 2.3 exige que la respuesta numerica siga visible y que se
            // diga por que no hay dibujo.
            showBanner("banner-warn", "Dibujo omitido", visualizer.overBudgetMessage(payload));
            showMessageCanvas("La respuesta esta calculada y visible a la derecha.");
            return;
        }

        canvasHolder.getChildren().setAll(visualizer.node());
        visualizer.render(payload);
        int steps = visualizer.prepareSteps(payload);
        playback.attach(visualizer::showStep, steps);
    }

    private void showIdleCanvas() {
        showMessageCanvas(mission.solver() == null
                ? "Esta mision todavia la esta implementando otro integrante."
                : "Carga el ejemplo y pulsa Resolver.");
    }

    private void showPendingCanvas() {
        showMessageCanvas("El dibujo de esta mision entra en la siguiente fase. "
                + "La respuesta numerica ya es correcta.");
    }

    private void showMessageCanvas(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("subtitle");
        label.setWrapText(true);
        label.setMaxWidth(420);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        StackPane holder = new StackPane(label);
        holder.setPadding(new Insets(30));
        canvasHolder.getChildren().setAll(holder);
        playback.detach();
    }

    private void showBanner(String styleClass, String heading, String detail) {
        Label title = new Label(heading);
        title.getStyleClass().add("title");
        title.setTextFill(switch (styleClass) {
            case "banner-error" -> Theme.ERROR;
            case "banner-info" -> Theme.MINERVA;
            default -> Theme.CHURUN;
        });

        Label body = new Label(detail);
        body.getStyleClass().add("subtitle");
        body.setWrapText(true);

        banner.getChildren().setAll(title, body);
        banner.getStyleClass().removeAll("banner-error", "banner-warn", "banner-info");
        banner.getStyleClass().add(styleClass);
        banner.setVisible(true);
        banner.setManaged(true);
    }

    private void clearBanner() {
        banner.getChildren().clear();
        banner.setVisible(false);
        banner.setManaged(false);
    }
}
