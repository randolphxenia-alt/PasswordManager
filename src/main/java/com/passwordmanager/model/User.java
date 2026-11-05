package com.passwordmanager.model;

/**
 * Зарегистрированный пользователь приложения (после успешного входа).
 *
 * @param id       идентификатор пользователя в БД
 * @param username имя пользователя
 */
public record User(int id, String username) {
}