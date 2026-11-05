package com.passwordmanager.model;

/**
 * Запись, подготовленная для отображения в интерфейсе.
 *
 * @param id            идентификатор записи в БД
 * @param data          данные записи в открытом виде
 * @param passwordState состояние пароля (доступен, хеш, повреждён)
 */
public record DecryptedEntry(int id, EntryData data, PasswordState passwordState) {
}