package com.passwordmanager.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
public enum Icon {
    SEARCH("M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0-14M20 20l-4-4"),
    PLUS("M12 5v14M5 12h14"),
    COPY("M9 9h10v10H9zM5 15V5h10"),
    EDIT("M4 20h4L19 9l-4-4L4 16zM13 7l4 4"),
    TRASH("M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6"),
    EYE("M2 12c3-6 17-6 20 0c-3 6-17 6-20 0zM12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6"),
    EYE_OFF("M2 12c3-6 17-6 20 0c-3 6-17 6-20 0zM12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6M4 4l16 16"),
    LOCK("M6 11h12v9H6zM8 11V8a4 4 0 0 1 8 0v3"),
    LOGOUT("M10 4H5v16h5M15 8l4 4l-4 4M19 12H9"),
    CHECK("M5 12l5 5l9-10"),
    USER("M12 4a4 4 0 1 0 0 8a4 4 0 1 0 0-8M4 20c0-4 4-6 8-6s8 2 8 6"),
    CLOSE("M6 6l12 12M18 6L6 18"),
    SHIELD("M12 3l8 3v6c0 5-4 8-8 9c-4-1-8-4-8-9V6zM9 12l2 2l4-4"),
    REFRESH("M20 12a8 8 0 1 1-2.3-5.7M20 4v5h-5"),
    ALERT("M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M12 8v5M12 16.5v.01"),
    GLOBE("M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3-3 15 0 18");
    private static final double GRID_SIZE = 24;
    private static final String STYLE_CLASS = "icon";
    private final String pathData;
    Icon(String pathData) {
        this.pathData = pathData;
    }
    public Node create(double size) {
        SVGPath path = new SVGPath();
        path.setContent(pathData);
        path.getStyleClass().add(STYLE_CLASS);
        Rectangle bounds = new Rectangle(GRID_SIZE, GRID_SIZE, Color.TRANSPARENT);
        Group scaled = new Group(bounds, path);
        scaled.setScaleX(size / GRID_SIZE);
        scaled.setScaleY(size / GRID_SIZE);
        Group icon = new Group(scaled);
        icon.setMouseTransparent(true);
        return icon;
    }
}