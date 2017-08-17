package com.faforever.gw.ui.universe;

import com.faforever.gw.model.entitity.SolarSystem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConnectionLine extends Line {
    private final SolarSystemController controllerA;
    private final SolarSystemController controllerB;


    private final SolarSystem solarSystemA;
    private final SolarSystem solarSystemB;

    public ConnectionLine(@NotNull SolarSystemController controllerA, @NotNull SolarSystemController controllerB) {
        this.controllerA = controllerA;
        this.controllerB = controllerB;

        solarSystemA = controllerA.getSolarSystem();
        solarSystemB = controllerB.getSolarSystem();

        Objects.requireNonNull(solarSystemA);
        Objects.requireNonNull(solarSystemB);

        if (solarSystemA == solarSystemB) {
            throw new IllegalArgumentException("A ConnectionLine can only connect two different SolarSystems");
        }

        setFill(Color.BLACK);
        setStrokeWidth(0.5);
        getStrokeDashArray().addAll(1.0, 3.0);

        Pane fromPane = controllerA.getRoot();
        Pane toPane = controllerB.getRoot();

        startXProperty().bind(fromPane.translateXProperty().add(fromPane.widthProperty().divide(2)));
        startYProperty().bind(fromPane.translateYProperty().add(fromPane.heightProperty().divide(2)));
        endXProperty().bind(toPane.translateXProperty().add(toPane.widthProperty().divide(2)));
        endYProperty().bind(toPane.translateYProperty().add(toPane.heightProperty().divide(2)));

        if (!controllerA.getConnectionLines().contains(this)) {
            controllerA.getConnectionLines().add(this);
        }
        if (!controllerB.getConnectionLines().contains(this)) {
            controllerB.getConnectionLines().add(this);
        }
    }

    public boolean connects(SolarSystem solarSystem) {
        return solarSystem == solarSystemA || solarSystem == solarSystemB;
    }

    public boolean connects(SolarSystem a, SolarSystem b) {
        return connects(a) && connects(b);
    }

    public void unlink() {
        controllerA.getConnectionLines().remove(this);
        controllerB.getConnectionLines().remove(this);
    }
}
