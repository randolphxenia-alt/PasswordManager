package com.passwordmanager.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.IntStream;

/**
 * Учебный блочный шифр на основе сети Фейстеля.
 * <p>
 * Параметры: блок 2 байта (левая и правая половины по 1 байту), 8 раундов,
 * раундовая функция {@code F(R, K) = (R + K) mod 256}, раундовый ключ —
 * очередной байт секретного ключа. Формат шифртекста:
 * {@code Base64( длина_исходника(4 байта) || зашифрованные_блоки )}.
 */
public final class FeistelCipher {

    /** Количество раундов сети Фейстеля. */
    private static final int ROUNDS = 8;

    /** Размер блока в байтах. */
    private static final int BLOCK_SIZE = 2;

    /** Размер заголовка с длиной исходных данных (int). */
    private static final int HEADER_SIZE = Integer.BYTES;

    /** Маска для приведения значения к одному байту. */
    private static final int BYTE_MASK = 0xFF;

    /**
     * Блок данных из двух половин.
     *
     * @param left  левая половина (0..255)
     * @param right правая половина (0..255)
     */
    private record Block(int left, int right) {
    }

    /** Закрытый конструктор: класс содержит только статические методы. */
    private FeistelCipher() {
    }

    /**
     * Шифрует строку.
     *
     * @param text открытый текст
     * @param key  секретный ключ (непустой)
     * @return шифртекст в Base64
     * @throws CryptoException если ключ пустой
     */
    public static String encrypt(String text, String key) {
        byte[] keyBytes = toKeyBytes(key);
        byte[] plain = text.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = transform(padToBlockSize(plain), keyBytes, true);
        byte[] payload = ByteBuffer.allocate(HEADER_SIZE + encrypted.length)
                .putInt(plain.length)
                .put(encrypted)
                .array();
        return Base64.getEncoder().encodeToString(payload);
    }

    /**
     * Расшифровывает строку, полученную методом {@link #encrypt(String, String)}.
     *
     * @param cipherText шифртекст в Base64
     * @param key        секретный ключ (непустой)
     * @return открытый текст
     * @throws CryptoException если данные повреждены или ключ пустой
     */
    public static String decrypt(String cipherText, String key) {
        byte[] keyBytes = toKeyBytes(key);
        ByteBuffer payload = ByteBuffer.wrap(decodeBase64(cipherText));
        if (payload.remaining() < HEADER_SIZE) {
            throw new CryptoException("Повреждённые данные: нет заголовка");
        }
        int originalLength = payload.getInt();
        byte[] encrypted = new byte[payload.remaining()];
        payload.get(encrypted);
        validateLayout(originalLength, encrypted.length);
        byte[] decrypted = transform(encrypted, keyBytes, false);
        return new String(decrypted, 0, originalLength, StandardCharsets.UTF_8);
    }

    /**
     * Преобразует ключ в байты и проверяет, что он не пустой.
     *
     * @param key секретный ключ
     * @return байты ключа в UTF-8
     * @throws CryptoException если ключ равен null или пуст
     */
    private static byte[] toKeyBytes(String key) {
        if (key == null || key.isEmpty()) {
            throw new CryptoException("Ключ шифрования пуст");
        }
        return key.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Декодирует Base64 с переводом ошибки в {@link CryptoException}.
     *
     * @param text строка в Base64
     * @return декодированные байты
     * @throws CryptoException если строка некорректна
     */
    private static byte[] decodeBase64(String text) {
        try {
            return Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Повреждённые данные: некорректный Base64", e);
        }
    }

    /**
     * Проверяет согласованность длины исходника и длины зашифрованных блоков.
     *
     * @param originalLength длина исходных данных из заголовка
     * @param encryptedLength длина зашифрованной части
     * @throws CryptoException если данные не могут быть корректным шифртекстом
     */
    private static void validateLayout(int originalLength, int encryptedLength) {
        boolean badLength = originalLength < 0 || originalLength > encryptedLength;
        boolean badPadding = encryptedLength % BLOCK_SIZE != 0
                || encryptedLength - originalLength >= BLOCK_SIZE;
        if (badLength || badPadding) {
            throw new CryptoException("Повреждённые данные: неверная длина");
        }
    }

    /**
     * Дополняет массив нулями до длины, кратной размеру блока.
     *
     * @param input исходные байты
     * @return новый массив длины, кратной {@link #BLOCK_SIZE}
     */
    private static byte[] padToBlockSize(byte[] input) {
        int paddedLength = (input.length + BLOCK_SIZE - 1) / BLOCK_SIZE * BLOCK_SIZE;
        byte[] padded = new byte[paddedLength];
        System.arraycopy(input, 0, padded, 0, input.length);
        return padded;
    }

    /**
     * Шифрует или расшифровывает все блоки массива.
     *
     * @param data     данные длины, кратной размеру блока
     * @param keyBytes байты ключа
     * @param encrypt  {@code true} — шифрование, {@code false} — расшифрование
     * @return новый массив с результатом
     */
    private static byte[] transform(byte[] data, byte[] keyBytes, boolean encrypt) {
        byte[] result = data.clone();
        IntStream.iterate(0, offset -> offset < result.length, offset -> offset + BLOCK_SIZE)
                .forEach(offset -> transformBlockAt(result, offset, keyBytes, encrypt));
        return result;
    }

    /**
     * Обрабатывает один блок массива на месте.
     *
     * @param data     массив данных
     * @param offset   смещение блока
     * @param keyBytes байты ключа
     * @param encrypt  направление преобразования
     */
    private static void transformBlockAt(byte[] data, int offset, byte[] keyBytes, boolean encrypt) {
        Block source = new Block(data[offset] & BYTE_MASK, data[offset + 1] & BYTE_MASK);
        Block result = runRounds(source, keyBytes, encrypt);
        data[offset] = (byte) result.left();
        data[offset + 1] = (byte) result.right();
    }

    /**
     * Прогоняет блок через все раунды сети Фейстеля.
     * При расшифровании раундовые ключи применяются в обратном порядке.
     *
     * @param block    исходный блок
     * @param keyBytes байты ключа
     * @param encrypt  направление преобразования
     * @return блок после всех раундов
     */
    private static Block runRounds(Block block, byte[] keyBytes, boolean encrypt) {
        return IntStream.range(0, ROUNDS)
                .map(step -> encrypt ? step : ROUNDS - 1 - step)
                .boxed()
                .reduce(block,
                        (current, round) -> applyRound(current, roundKey(keyBytes, round), encrypt),
                        (first, second) -> second);
    }

    /**
     * Выполняет один раунд шифрования или расшифрования.
     * <ul>
     *     <li>шифрование: {@code (L, R) -> (R, L xor F(R, K))}</li>
     *     <li>расшифрование: {@code (L, R) -> (R xor F(L, K), L)}</li>
     * </ul>
     *
     * @param block    текущий блок
     * @param roundKey раундовый ключ
     * @param encrypt  направление преобразования
     * @return блок после раунда
     */
    private static Block applyRound(Block block, int roundKey, boolean encrypt) {
        if (encrypt) {
            return new Block(block.right(), block.left() ^ roundFunction(block.right(), roundKey));
        }
        return new Block(block.right() ^ roundFunction(block.left(), roundKey), block.left());
    }

    /**
     * Раундовая функция {@code F(half, K) = (half + K) mod 256}.
     *
     * @param half     половина блока
     * @param roundKey раундовый ключ
     * @return значение функции (0..255)
     */
    private static int roundFunction(int half, int roundKey) {
        return (half + roundKey) & BYTE_MASK;
    }

    /**
     * Возвращает раундовый ключ — байт ключа с номером {@code round mod длина_ключа}.
     *
     * @param keyBytes байты ключа
     * @param round    номер раунда
     * @return раундовый ключ (0..255)
     */
    private static int roundKey(byte[] keyBytes, int round) {
        return keyBytes[round % keyBytes.length] & BYTE_MASK;
    }
}