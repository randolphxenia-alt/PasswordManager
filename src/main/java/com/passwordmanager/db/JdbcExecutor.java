package com.passwordmanager.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Вспомогательный класс для выполнения SQL-запросов.
 * <p>
 * Убирает из DAO повторяющийся код: открытие соединения, подготовку запроса,
 * подстановку параметров и закрытие ресурсов (try-with-resources).
 */
public final class JdbcExecutor {

    /**
     * Преобразователь строки результата запроса в объект.
     *
     * @param <T> тип получаемого объекта
     */
    @FunctionalInterface
    public interface RowMapper<T> {

        /**
         * Преобразует текущую строку {@link ResultSet} в объект.
         *
         * @param resultSet результат запроса, установленный на нужную строку
         * @return объект, построенный из строки
         * @throws SQLException при ошибке чтения столбцов
         */
        T map(ResultSet resultSet) throws SQLException;
    }

    /** Закрытый конструктор: класс содержит только статические методы. */
    private JdbcExecutor() {
    }

    /**
     * Выполняет запрос INSERT / UPDATE / DELETE.
     *
     * @param sql    SQL-запрос с параметрами {@code ?}
     * @param params значения параметров по порядку
     * @return количество изменённых строк
     * @throws SQLException при ошибке выполнения запроса
     */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, params);
            return statement.executeUpdate();
        }
    }

    /**
     * Выполняет запрос SELECT и возвращает все строки результата.
     *
     * @param sql    SQL-запрос с параметрами {@code ?}
     * @param mapper преобразователь строки в объект
     * @param params значения параметров по порядку
     * @param <T>    тип объектов результата
     * @return список объектов (может быть пустым)
     * @throws SQLException при ошибке выполнения запроса
     */
    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, params);
            return readAll(statement, mapper);
        }
    }

    /**
     * Выполняет запрос SELECT и возвращает первую строку результата.
     *
     * @param sql    SQL-запрос с параметрами {@code ?}
     * @param mapper преобразователь строки в объект
     * @param params значения параметров по порядку
     * @param <T>    тип объекта результата
     * @return первый объект или пустой {@link Optional}, если строк нет
     * @throws SQLException при ошибке выполнения запроса
     */
    public static <T> Optional<T> queryFirst(String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        return query(sql, mapper, params).stream().findFirst();
    }

    /**
     * Подставляет значения в параметры подготовленного запроса.
     *
     * @param statement подготовленный запрос
     * @param params    значения параметров
     * @throws SQLException при ошибке подстановки
     */
    private static void bindParameters(PreparedStatement statement, Object... params)
            throws SQLException {
        for (int index = 0; index < params.length; index++) {
            statement.setObject(index + 1, params[index]);
        }
    }

    /**
     * Читает все строки результата запроса.
     *
     * @param statement подготовленный запрос с подставленными параметрами
     * @param mapper    преобразователь строки в объект
     * @param <T>       тип объектов
     * @return список объектов
     * @throws SQLException при ошибке чтения
     */
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