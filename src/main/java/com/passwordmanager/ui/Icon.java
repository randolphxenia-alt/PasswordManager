package com.passwordmanager.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 * Векторные иконки интерфейса (контурные, сетка 24×24).
 * Цвет и толщина линий задаются в CSS через класс {@code .icon},
 * поэтому иконки выглядят одинаково чётко на любом экране и меняют цвет при наведении.
 */
public enum Icon {

    /** Лупа (поиск). */
    SEARCH("M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0-14M20 20l-4-4"),

    /** Плюс (добавление). */
    PLUS("M12 5v14M5 12h14"),

    /** Два листа (копирование). */
    COPY("M9 9h10v10H9zM5 15V5h10"),

    /** Карандаш (изменение). */
    EDIT("M4 20h4L19 9l-4-4L4 16zM13 7l4 4"),

    /** Корзина (удаление). */
    TRASH("M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6"),

    /** Глаз (показать пароль). */
    EYE("M2 12c3-6 17-6 20 0c-3 6-17 6-20 0zM12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6"),

    /** Перечёркнутый глаз (скрыть пароль). */
    EYE_OFF("M2 12c3-6 17-6 20 0c-3 6-17 6-20 0zM12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6M4 4l16 16"),

    /** Замок (логотип). */
    LOCK("M6 11h12v9H6zM8 11V8a4 4 0 0 1 8 0v3"),

    /** Дверь со стрелкой (выход). */
    LOGOUT("M10 4H5v16h5M15 8l4 4l-4 4M19 12H9"),

    /** Галочка (успех). */
    CHECK("M5 12l5 5l9-10"),

    /** Человек (пользователь). */
    USER("M12 4a4 4 0 1 0 0 8a4 4 0 1 0 0-8M4 20c0-4 4-6 8-6s8 2 8 6"),

    /** Крестик (закрыть). */
    CLOSE("M6 6l12 12M18 6L6 18"),

    /** Щит с галочкой (проверка пароля). */
    SHIELD("M12 3l8 3v6c0 5-4 8-8 9c-4-1-8-4-8-9V6zM9 12l2 2l4-4"),

    /** Круговая стрелка (генерация пароля). */
    REFRESH("M20 12a8 8 0 1 1-2.3-5.7M20 4v5h-5"),

    /** Восклицательный знак в круге (ошибка). */
    ALERT("M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M12 8v5M12 16.5v.01"),

    /** Глобус (сайт). */
    GLOBE("M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3-3 15 0 18");

    /** Размер сетки, в которой нарисованы иконки. */
    private static final double GRID_SIZE = 24;

    /** CSS-класс контура иконки. */
    private static final String STYLE_CLASS = "icon";

    /** Данные SVG-контура. */
    private final String pathData;

    /**
     * Создаёт иконку.
     *
     * @param pathData данные SVG-контура
     */
    Icon(String pathData) {
        this.pathData = pathData;
    }

    /**
     * Создаёт узел иконки нужного размера.
     *
     * @param size размер стороны в пикселях
     * @return узел иконки
     */
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