package com.passwordmanager.service;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.db.DatabaseConnection;
import com.passwordmanager.db.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Базовый класс тестов, работающих с БД: каждый тест получает
 * собственную пустую базу во временном каталоге.
 */
abstract class DatabaseTestBase {

    /** Временный каталог, удаляемый JUnit после теста. */
    @TempDir
    Path tempDir;

    /**
     * Создаёт пустую тестовую БД.
     *
     * @throws SQLException если схему создать не удалось
     */
    @BeforeEach
    void setUpDatabase() throws SQLException {
        DatabaseConnection.setUrl("jdbc:sqlite:" + tempDir.resolve("test.db"));
        DatabaseInitializer.initialize();
    }

    /** Возвращает рабочую БД приложения. */
    @AfterEach
    void restoreDatabase() {
        DatabaseConnection.setUrl(AppConfig.DATABASE_URL);
    }
}