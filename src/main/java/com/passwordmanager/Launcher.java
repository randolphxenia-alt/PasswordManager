package com.passwordmanager;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Точка входа для запуска из jar-файла.
 * <p>
 * Главный класс fat-jar не должен наследоваться от {@link javafx.application.Application}:
 * иначе JVM при запуске {@code java -jar} сообщает «JavaFX runtime components are missing».
 * Поэтому запуск выполняется через этот промежуточный класс.
 */
public final class Launcher {

    /** Системное свойство с кодировкой консоли; читается в {@code log4j2.xml}. */
    static final String CONSOLE_ENCODING_PROPERTY = "console.encoding";

    /** Закрытый конструктор: класс содержит только статические методы. */
    private Launcher() {
    }

    /**
     * Определяет кодировку консоли и запускает JavaFX-приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        System.setProperty(CONSOLE_ENCODING_PROPERTY, detectConsoleCharset().name());
        PasswordManagerApp.main(args);
    }

    /**
     * Определяет кодировку консоли, в которой запущено приложение
     * (в Windows это обычно CP866), чтобы кириллица в логе читалась без {@code chcp}.
     * Если консоли нет (запуск из IDE или Gradle), используется UTF-8.
     *
     * @return кодировка консоли
     */
    static Charset detectConsoleCharset() {
        return Optional.ofNullable(System.console())
                .map(Console::charset)
                .orElse(StandardCharsets.UTF_8);
    }
}