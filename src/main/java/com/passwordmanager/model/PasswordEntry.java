package com.passwordmanager.model;

/**
 * Запись менеджера паролей в том виде, в котором она хранится в БД
 * (пароль защищён выбранным способом).
 *
 * @param id           идентификатор записи
 * @param userId       идентификатор владельца
 * @param title        название записи
 * @param website      адрес сайта (может быть пустым)
 * @param login        логин на сайте
 * @param passwordData защищённый пароль
 * @param storageMode  способ защиты пароля
 */
public record PasswordEntry(int id, int userId, String title, String website,
                            String login, String passwordData, StorageMode storageMode) {
}