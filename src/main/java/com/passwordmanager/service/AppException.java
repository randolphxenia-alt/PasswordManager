package com.passwordmanager.service;

/**
 * Ожидаемая ошибка бизнес-логики, сообщение которой можно показать пользователю
 * (неверный ввод, занятое имя, неверный пароль, недоступная БД и т.п.).
 */
public class AppException extends Exception {

    /** Версия сериализованной формы. */
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с понятным пользователю сообщением.
     *
     * @param message сообщение для пользователя
     */
    public AppException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и исходной причиной.
     *
     * @param message сообщение для пользователя
     * @param cause   исходное исключение
     */
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}