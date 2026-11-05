package com.passwordmanager.model;

/**
 * Данные записи в открытом виде: то, что пользователь вводит в форме
 * и что видит на карточке после расшифровки.
 *
 * @param title       название записи
 * @param website     адрес сайта (может быть пустым)
 * @param login       логин на сайте
 * @param password    пароль (пустой, если он недоступен или не меняется)
 * @param storageMode способ защиты пароля
 */
public record EntryData(String title, String website, String login, String password,
                        StorageMode storageMode) {
}