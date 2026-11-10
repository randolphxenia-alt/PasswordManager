package com.passwordmanager.service;

import com.passwordmanager.config.AppConfig;
import com.passwordmanager.db.DatabaseConnection;
import com.passwordmanager.db.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;





abstract class DatabaseTestBase {


    @TempDir
    Path tempDir;






    @BeforeEach
    void setUpDatabase() throws SQLException {
        DatabaseConnection.setUrl("jdbc:sqlite:" + tempDir.resolve("test.db"));
        DatabaseInitializer.initialize();
    }


    @AfterEach
    void restoreDatabase() {
        DatabaseConnection.setUrl(AppConfig.DATABASE_URL);
    }
}