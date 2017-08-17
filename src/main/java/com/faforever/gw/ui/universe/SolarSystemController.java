package com.faforever.gw.ui.universe;

import com.faforever.gw.model.entitity.SolarSystem;
import com.faforever.gw.ui.Controller;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@Scope("prototype")
@Slf4j
public class SolarSystemController implements Controller<Pane> {
    private final static double UNIVERSE_WIDTH = 100;
    private final static double UNIVERSE_HEIGHT = 100;

    private final static Color DEFAULT_COLOR = Color.BLACK;
    private final static Color HOVER_COLOR = Color.RED;
    private final static Color SELECTED_COLOR = Color.GREEN;

    @Getter
    @NonNull
    private SolarSystem solarSystem;
    @FXML
    private StackPane solarSystemRoot;
    @FXML
    private Label coordinatesLabel;
    @FXML
    private Circle planetCircle;

    private boolean isHovered = false;
    private boolean isSelected = false;


    public void setSolarSystem(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;

        coordinatesLabel.setText(MessageFormat.format("({0},{1},{2})", solarSystem.getX(), solarSystem.getY(), solarSystem.getZ()));

        planetCircle.setFill(DEFAULT_COLOR);

        planetCircle.setOnMouseEntered(event -> {
            isHovered = true;
            invalidate();
        });

        planetCircle.setOnMouseExited(event -> {
            isHovered = false;
            invalidate();
        });

        planetCircle.setOnMouseClicked(event -> {
            isSelected = !isSelected;
            invalidate();
        });


        Pane parent = (Pane) getRoot().getParent();

//        double sideLength = 50;
//        double scale = 0.25;
//        double canvasSize = parent.getWidth();
//
//        solarSystemRoot.setTranslateX(solarSystem.getX() * sideLength * scale + sideLength / 2 * (scale - 1));
//        solarSystemRoot.setTranslateY(solarSystem.getY() * sideLength * scale + sideLength / 2 * (scale - 1));
//        solarSystemRoot.setScaleX(scale);
//        solarSystemRoot.setScaleY(scale);

        ReadOnlyDoubleProperty sideLengthProperty = solarSystemRoot.widthProperty();
        DoubleBinding scaleX = parent.widthProperty().divide(UNIVERSE_WIDTH).divide(sideLengthProperty);
        DoubleBinding scaleY = parent.heightProperty().divide(UNIVERSE_HEIGHT).divide(sideLengthProperty);


        DoubleBinding offsetX = sideLengthProperty.divide(2).multiply(scaleX.subtract(1));
        DoubleBinding offsetY = sideLengthProperty.divide(2).multiply(scaleY.subtract(1));
        solarSystemRoot.translateXProperty().bind(sideLengthProperty.multiply(scaleX).multiply(solarSystem.getX()).add(offsetX));
        solarSystemRoot.translateYProperty().bind(sideLengthProperty.multiply(scaleY).multiply(solarSystem.getY()).add(offsetY));
        solarSystemRoot.scaleXProperty().bind(scaleX);
        solarSystemRoot.scaleYProperty().bind(scaleY);
    }

    @Override
    public Pane getRoot() {
        return solarSystemRoot;
    }


    private void invalidate() {
        if (isSelected) {
            planetCircle.setFill(SELECTED_COLOR);
        } else if (isHovered) {
            planetCircle.setFill(HOVER_COLOR);
        } else {
            planetCircle.setFill(DEFAULT_COLOR);
        }
    }
}
