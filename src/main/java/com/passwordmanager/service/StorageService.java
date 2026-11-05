package com.passwordmanager.service;

import com.passwordmanager.crypto.Base64Service;
import com.passwordmanager.crypto.CryptoException;
import com.passwordmanager.crypto.FeistelCipher;
import com.passwordmanager.crypto.KeyVault;
import com.passwordmanager.crypto.MD5Service;
import com.passwordmanager.crypto.SaltedHashService;
import com.passwordmanager.model.StorageMode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * Фасад над криптографическими классами: единая точка защиты, восстановления
 * и проверки паролей записей для любого {@link StorageMode}.
 * Секретные ключи берутся из {@link KeyVault} и пользователю не показываются.
 */
public final class StorageService {

    /** Закрытый конструктор: класс содержит только статические методы. */
    private StorageService() {
    }

    /**
     * Защищает пароль выбранным способом.
     *
     * @param password пароль в открытом виде
     * @param mode     способ защиты
     * @return данные для сохранения в БД
     * @throws CryptoException при ошибке криптографической операции
     */
    public static String encode(String password, StorageMode mode) {
        return switch (mode) {
            case BASE64 -> Base64Service.encode(password);
            case MD5 -> MD5Service.hash(password);
            case FEISTEL -> FeistelCipher.encrypt(password, KeyVault.feistelKey());
            case SALTED_HASH -> SaltedHashService.hash(password, KeyVault.saltPepper());
        };
    }

    /**
     * Восстанавливает пароль. Для необратимых способов возвращает пустой {@link Optional}.
     *
     * @param stored данные из БД
     * @param mode   способ защиты
     * @return пароль в открытом виде или пустой {@link Optional}
     * @throws CryptoException если данные повреждены
     */
    public static Optional<String> decode(String stored, StorageMode mode) {
        return switch (mode) {
            case BASE64 -> Optional.of(Base64Service.decode(stored));
            case FEISTEL -> Optional.of(FeistelCipher.decrypt(stored, KeyVault.feistelKey()));
            case MD5, SALTED_HASH -> Optional.empty();
        };
    }

    /**
     * Проверяет, совпадает ли введённый пароль с сохранённым.
     * Работает для всех способов: обратимые данные расшифровываются, для хешей сравниваются хеши.
     *
     * @param candidate проверяемый пароль
     * @param stored    данные из БД
     * @param mode      способ защиты
     * @return {@code true}, если пароли совпадают
     * @throws CryptoException если сохранённые данные повреждены
     */
    public static boolean matches(String candidate, String stored, StorageMode mode) {
        return switch (mode) {
            case BASE64, FEISTEL -> decode(stored, mode)
                    .map(password -> constantTimeEquals(password, candidate))
                    .orElse(false);
            case MD5 -> constantTimeEquals(MD5Service.hash(candidate), stored);
            case SALTED_HASH -> SaltedHashService.verify(candidate, stored, KeyVault.saltPepper());
        };
    }

    /**
     * Сравнивает строки за постоянное время (защита от атак по времени).
     *
     * @param first  первая строка
     * @param second вторая строка
     * @return {@code true}, если строки равны
     */
    private static boolean constantTimeEquals(String first, String second) {
        return MessageDigest.isEqual(
                first.getBytes(StandardCharsets.UTF_8),
                second.getBytes(StandardCharsets.UTF_8));
    }
}