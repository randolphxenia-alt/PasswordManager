package com.passwordmanager.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcExecutor {

    @FunctionalInterface
    public interface RowMapper<T> {

        T map(ResultSet resultSet) throws SQLException;
    }

    private JdbcExecutor() {
    }

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, params);
            return statement.executeUpdate();
        }
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, params);
            return readAll(statement, mapper);
        }
    }

    public static <T> Optional<T> queryFirst(String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        return query(sql, mapper, params).stream().findFirst();
    }

    private static void bindParameters(PreparedStatement statement, Object... params)
            throws SQLException {
        for (int index = 0; index < params.length; index++) {
            statement.setObject(index + 1, params[index]);
        }
    }

    private static <T> List<T> readAll(PreparedStatement statement, RowMapper<T> mapper)
            throws SQLException {
        List<T> rows = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(mapper.map(resultSet));
            }
        }
        return rows;
    }
}