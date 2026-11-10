package com.passwordmanager.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.IntStream;
public final class FeistelCipher {
    private static final int ROUNDS = 8;
    private static final int BLOCK_SIZE = 2;
    private static final int HEADER_SIZE = Integer.BYTES;
    private static final int BYTE_MASK = 0xFF;
    private record Block(int left, int right) {
    }
    private FeistelCipher() {
    }
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
    private static byte[] toKeyBytes(String key) {
        if (key == null || key.isEmpty()) {
            throw new CryptoException("Ключ шифрования пуст");
        }
        return key.getBytes(StandardCharsets.UTF_8);
    }
    private static byte[] decodeBase64(String text) {
        try {
            return Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Повреждённые данные: некорректный Base64", e);
        }
    }
    private static void validateLayout(int originalLength, int encryptedLength) {
        boolean badLength = originalLength < 0 || originalLength > encryptedLength;
        boolean badPadding = encryptedLength % BLOCK_SIZE != 0
                || encryptedLength - originalLength >= BLOCK_SIZE;
        if (badLength || badPadding) {
            throw new CryptoException("Повреждённые данные: неверная длина");
        }
    }
    private static byte[] padToBlockSize(byte[] input) {
        int paddedLength = (input.length + BLOCK_SIZE - 1) / BLOCK_SIZE * BLOCK_SIZE;
        byte[] padded = new byte[paddedLength];
        System.arraycopy(input, 0, padded, 0, input.length);
        return padded;
    }
    private static byte[] transform(byte[] data, byte[] keyBytes, boolean encrypt) {
        byte[] result = data.clone();
        IntStream.iterate(0, offset -> offset < result.length, offset -> offset + BLOCK_SIZE)
                .forEach(offset -> transformBlockAt(result, offset, keyBytes, encrypt));
        return result;
    }
    private static void transformBlockAt(byte[] data, int offset, byte[] keyBytes, boolean encrypt) {
        Block source = new Block(data[offset] & BYTE_MASK, data[offset + 1] & BYTE_MASK);
        Block result = runRounds(source, keyBytes, encrypt);
        data[offset] = (byte) result.left();
        data[offset + 1] = (byte) result.right();
    }
    private static Block runRounds(Block block, byte[] keyBytes, boolean encrypt) {
        return IntStream.range(0, ROUNDS)
                .map(step -> encrypt ? step : ROUNDS - 1 - step)
                .boxed()
                .reduce(block,
                        (current, round) -> applyRound(current, roundKey(keyBytes, round), encrypt),
                        (first, second) -> second);
    }
    private static Block applyRound(Block block, int roundKey, boolean encrypt) {
        if (encrypt) {
            return new Block(block.right(), block.left() ^ roundFunction(block.right(), roundKey));
        }
        return new Block(block.right() ^ roundFunction(block.left(), roundKey), block.left());
    }
    private static int roundFunction(int half, int roundKey) {
        return (half + roundKey) & BYTE_MASK;
    }
    private static int roundKey(byte[] keyBytes, int round) {
        return keyBytes[round % keyBytes.length] & BYTE_MASK;
    }
}