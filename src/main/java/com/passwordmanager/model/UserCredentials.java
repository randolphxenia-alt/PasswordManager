package com.passwordmanager.model;

/**
 * Учётные данные пользователя в том виде, в котором они хранятся в БД.
 *
 * @param id           идентификатор пользователя
 * @param username     имя пользователя
 * @param passwordHash PBKDF2-хеш мастер-пароля в формате {@code соль:хеш}
 */
public record UserCredentials(int id, String username, String passwordHash) {

    /**
     * Возвращает пользователя без конфиденциальных данных.
     *
     * @return объект {@link User}
     */
    public User toUser() {
        return new User(id, username);
    }
}