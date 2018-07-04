import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VariabilityGui extends Application {
    private Variability variability;
    private final FileChooser fileChooser;
    private Stage primaryStage;
    private TextField textMeanValue;
    private TextField textStdDeviation;
    private TextField textVariability;

    public VariabilityGui() {
        fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("TXT", "*.txt");
        final FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPG", "*.jpg");
        fileChooser.getExtensionFilters().addAll(txtFilter, jpgFilter);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        BorderPane border = new BorderPane();

        HBox hbox = addHBox();
        border.setTop(hbox);

        GridPane gridPane = addGridPane();
        border.setCenter(gridPane);
        border.setPadding(new Insets(0, 0, 10, 0));

        addStackPane(hbox);
        hbox.setOnMouseClicked(
                event -> {
                    alertDialog();
                }
        );


        Scene scene = new Scene(border);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CL calculation");
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(visualBounds.getMaxX() - 600);
        primaryStage.setY((visualBounds.getHeight() - primaryStage.getHeight()) / 4);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #cfdae2;");

        Button openButton = new Button("Open Image");
        openButton.setPrefSize(100, 20);
        openButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if ((file != null)) {
                        String fileName = file.getName();
                        String fileExtension = fileName.substring(fileName.indexOf(".") + 1, file.getName().length());
                        if (fileExtension.equals("txt")) {
                            variability = new Variability(file, false);
                            textMeanValue.setText(String.valueOf(variability.getMeanVal()));
                            textStdDeviation.setText(String.valueOf(variability.getStdDeviation()));
                            textVariability.setText(String.valueOf(variability.getCoeffVariation()));
                        } else if (fileExtension.equals("jpg")) {
                            variability = new Variability(file);
                            openImage(file, variability.getArr().length);
                            textMeanValue.setText(String.valueOf(variability.getMeanVal()));
                            textStdDeviation.setText(String.valueOf(variability.getStdDeviation()));
                            textVariability.setText(String.valueOf(variability.getCoeffVariation()));
                        }
                    }
                });

        Button saveButton = new Button("Save File");
        saveButton.setPrefSize(100, 20);
        saveButton.setOnAction(
                e -> {
                    fileChooser.getExtensionFilters().removeAll();
                    if (variability != null) {
                        File file = fileChooser.showSaveDialog(primaryStage);
                        if (file != null) {
                            variability.saveNormalDistribution(file, variability.getArr());
                            variability.findExtremes();
                            showNormalDistribution(variability.getArr(), variability.getNormalDistArray(),
                                    variability.getxMin(), variability.getxMax(),
                                    variability.getyMin(), variability.getyMax()
                            );
                        }
                    } else {
                        errorDialog();
                    }
                });
        hbox.getChildren().addAll(openButton, saveButton);

        return hbox;
    }

    private void showNormalDistribution(float array[][], float[][] normalArray, float xMin, float xMax,
                                        float yMin, float yMax) {
        Stage stage = new Stage();
        VBox box = new VBox();
        box.setPadding(new Insets(10));

        float xTick = (xMax - xMin) / 10;
        float yTick = (yMax - yMin) / 10;
        NumberAxis xAxis = new NumberAxis(xMin - xTick, xMax + xTick, xTick);
        NumberAxis yAxis = new NumberAxis(yMin - yTick, yMax + yTick, yTick);

        final ScatterChart<Number, Number> chart = new ScatterChart<>(xAxis, yAxis);
        xAxis.setLabel("X");
        yAxis.setLabel("Density probability");
        chart.setTitle("Normal Distribution");
        chart.snappedRightInset();
        XYChart.Series series1 = new XYChart.Series();

        String legend = String.format("µ = %1$.3f σ = %2$.3f Variance =  %3$.3f ",
                variability.getMeanVal(),
                variability.getStdDeviation(),
                variability.getCoeffVariation());
        series1.setName(legend);
        for (int i = 0; i < array.length; i += 20) {
            for (int j = 0; j < array.length; j += 20) {
                series1.getData().add(new XYChart.Data(array[i][j], normalArray[i][j]));
            }
        }

        chart.getData().addAll(series1);
        chart.setMinSize(750, 550);
        box.getChildren().add(chart);
        Scene scene = new Scene(box, 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void openImage(File file, int size) {
        Stage stage = new Stage();
        VBox box = new VBox();
        ImageView imageView = null;
        try {
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            if (size > 512) {
                size = 512;
                imageView.setFitHeight(size);
                imageView.setFitWidth(size);
            }
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        box.getChildren().add(imageView);
        Scene scene = new Scene(box, size, size);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void addStackPane(HBox hb) {
        StackPane stack = new StackPane();
        Rectangle helpIcon = new Rectangle(30.0, 25.0);
        helpIcon.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4977A3")),
                new Stop(0.5, Color.web("#B0C6DA")),
                new Stop(1, Color.web("#9CB6CF"))));
        helpIcon.setStroke(Color.web("#D0E6FA"));
        helpIcon.setArcHeight(3.5);
        helpIcon.setArcWidth(3.5);

        Text helpText = new Text("?");
        helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        helpText.setFill(Color.WHITE);
        helpText.setStroke(Color.web("#7080A0"));

        stack.getChildren().addAll(helpIcon, helpText);
        stack.setAlignment(Pos.CENTER_RIGHT);
        // Add offset to right for question mark to compensate for RIGHT
        // alignment of all nodes
        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0));

        hb.getChildren().add(stack);
        HBox.setHgrow(stack, Priority.ALWAYS);

    }

    private GridPane addGridPane() {
        final GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 0, 10));
        grid.setHgap(10);
        grid.setVgap(10);

        Label label = new Label("Mean value:");
        Label label2 = new Label("Standard deviation:");
        Label label3 = new Label("CL variability, %:");

        textMeanValue = new TextField();
        textMeanValue.editableProperty().setValue(false);
        textMeanValue.setMaxSize(100, 20);
        textStdDeviation = new TextField();
        textStdDeviation.editableProperty().setValue(false);
        textStdDeviation.setMaxSize(100, 20);
        textVariability = new TextField();
        textVariability.editableProperty().setValue(false);
        textVariability.setMaxSize(100, 20);

        grid.add(label, 0, 0);
        grid.add(label2, 0, 1);
        grid.add(label3, 0, 2);

        grid.add(textMeanValue, 1, 0);
        grid.add(textStdDeviation, 1, 1);
        grid.add(textVariability, 1, 2);
        return grid;
    }

    private void alertDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Image variability and normal distribution");
        String s = "Kolchiba Nikita\nShizuoka University, Prof. Kawata's lab, Japan";
        alert.setContentText(s);
        alert.show();
    }

    private void errorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Count variability first!");
        alert.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}