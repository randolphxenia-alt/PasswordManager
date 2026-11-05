package com.passwordmanager.crypto;

/**
 * Ошибка криптографической операции: повреждённые данные, неверный ключ,
 * недоступный алгоритм и т.п.
 */
public class CryptoException extends RuntimeException {

    /** Версия сериализованной формы. */
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message описание ошибки
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и исходной причиной.
     *
     * @param message описание ошибки
     * @param cause   исходное исключение
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}