package com.passwordmanager;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class Launcher {
    static final String CONSOLE_ENCODING_PROPERTY = "console.encoding";
    private Launcher() {
    }
    public static void main(String[] args) {
        System.setProperty(CONSOLE_ENCODING_PROPERTY, detectConsoleCharset().name());
        PasswordManagerApp.main(args);
    }

    static Charset detectConsoleCharset() {
        return Optional.ofNullable(System.console())
                .map(Console::charset)
                .orElse(StandardCharsets.UTF_8);
    }
}