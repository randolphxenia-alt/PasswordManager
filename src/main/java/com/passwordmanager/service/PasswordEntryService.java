package com.passwordmanager.service;

import com.passwordmanager.crypto.CryptoException;
import com.passwordmanager.dao.PasswordEntryDAO;
import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.model.EntryData;
import com.passwordmanager.model.PasswordEntry;
import com.passwordmanager.model.PasswordState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class PasswordEntryService {

    private static final Logger log = LogManager.getLogger(PasswordEntryService.class);

    private static final Comparator<DecryptedEntry> DISPLAY_ORDER =
            Comparator.comparing((DecryptedEntry entry) -> entry.data().title(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(DecryptedEntry::id);

    private final PasswordEntryDAO entryDAO;
    public PasswordEntryService() {
        this(new PasswordEntryDAO());
    }

    public PasswordEntryService(PasswordEntryDAO entryDAO) {
        this.entryDAO = entryDAO;
    }

    public List<DecryptedEntry> findEntries(int userId, String query) throws AppException {
        String needle = InputValidator.normalize(query).toLowerCase(Locale.ROOT);
        try {
            return entryDAO.findAllByUser(userId).stream()
                    .filter(entry -> matchesQuery(entry, needle))
                    .map(this::decrypt)
                    .sorted(DISPLAY_ORDER)
                    .toList();
        } catch (SQLException e) {
            log.error("Ошибка загрузки записей пользователя {}", userId, e);
            throw new AppException("Не удалось загрузить записи", e);
        }
    }

    public void addEntry(int userId, EntryData data) throws AppException {
        EntryData clean = normalize(data);
        InputValidator.validateEntry(clean, true);
        try {
            String passwordData = StorageService.encode(clean.password(), clean.storageMode());
            entryDAO.add(toStored(0, userId, clean, passwordData));
            log.info("Пользователь {} добавил запись «{}» ({})", userId, clean.title(), clean.storageMode().name());
        } catch (SQLException | CryptoException e) {
            log.error("Ошибка добавления записи", e);
            throw new AppException("Не удалось сохранить запись", e);
        }
    }

    public void updateEntry(int userId, int entryId, EntryData data) throws AppException {
        EntryData clean = normalize(data);
        InputValidator.validateEntry(clean, false);
        try {
            PasswordEntry current = loadEntry(userId, entryId);
            String passwordData = resolvePasswordData(current, clean);
            requireFound(entryDAO.update(toStored(entryId, userId, clean, passwordData)), entryId);
            log.info("Пользователь {} изменил запись {}", userId, entryId);
        } catch (SQLException | CryptoException e) {
            log.error("Ошибка изменения записи {}", entryId, e);
            throw new AppException("Не удалось изменить запись", e);
        }
    }

    public void deleteEntry(int userId, int entryId) throws AppException {
        try {
            requireFound(entryDAO.delete(userId, entryId), entryId);
            log.info("Пользователь {} удалил запись {}", userId, entryId);
        } catch (SQLException e) {
            log.error("Ошибка удаления записи {}", entryId, e);
            throw new AppException("Не удалось удалить запись", e);
        }
    }

    public boolean verifyPassword(int userId, int entryId, String candidate) throws AppException {
        if (candidate == null || candidate.isEmpty()) {
            throw new AppException("Введите пароль для проверки");
        }
        try {
            PasswordEntry entry = loadEntry(userId, entryId);
            boolean matches = StorageService.matches(candidate, entry.passwordData(), entry.storageMode());
            log.info("Проверка пароля записи {}: {}", entryId, matches ? "совпадает" : "не совпадает");
            return matches;
        } catch (SQLException | CryptoException e) {
            log.error("Ошибка проверки пароля записи {}", entryId, e);
            throw new AppException("Не удалось проверить пароль: данные повреждены", e);
        }
    }

    private PasswordEntry loadEntry(int userId, int entryId) throws AppException, SQLException {
        return entryDAO.findById(userId, entryId).orElseThrow(() -> new AppException("Запись не найдена"));
    }

    private static String resolvePasswordData(PasswordEntry current, EntryData data) throws AppException {
        if (!data.password().isEmpty()) {
            return StorageService.encode(data.password(), data.storageMode());
        }
        if (current.storageMode() == data.storageMode()) {
            return current.passwordData();
        }
        String password = StorageService.decode(current.passwordData(), current.storageMode())
                .orElseThrow(() -> new AppException(
                        "Пароль сохранён хешем: чтобы сменить способ защиты, введите пароль заново"));
        return StorageService.encode(password, data.storageMode());
    }

    private DecryptedEntry decrypt(PasswordEntry entry) {
        try {
            return StorageService.decode(entry.passwordData(), entry.storageMode())
                    .map(password -> toDecrypted(entry, password, PasswordState.AVAILABLE))
                    .orElseGet(() -> toDecrypted(entry, "", PasswordState.HASHED));
        } catch (CryptoException e) {
            log.warn("Не удалось расшифровать запись {}: {}", entry.id(), e.getMessage());
            return toDecrypted(entry, "", PasswordState.CORRUPTED);
        }
    }

    private static DecryptedEntry toDecrypted(PasswordEntry entry, String password, PasswordState state) {
        EntryData data = new EntryData(entry.title(), entry.website(), entry.login(), password, entry.storageMode());
        return new DecryptedEntry(entry.id(), data, state);
    }

    private static boolean matchesQuery(PasswordEntry entry, String needle) {
        return needle.isEmpty() || Stream.of(entry.title(), entry.website(), entry.login())
                .anyMatch(text -> text != null && text.toLowerCase(Locale.ROOT).contains(needle));
    }

    private static EntryData normalize(EntryData data) {
        return new EntryData(
                InputValidator.normalize(data.title()),
                InputValidator.normalize(data.website()),
                InputValidator.normalize(data.login()),
                data.password() == null ? "" : data.password(),
                data.storageMode());
    }

    private static PasswordEntry toStored(int entryId, int userId, EntryData data, String passwordData) {
        return new PasswordEntry(entryId, userId, data.title(), data.website(), data.login(),
                passwordData, data.storageMode());
    }

    private static void requireFound(boolean found, int entryId) throws AppException {
        if (!found) {
            log.warn("Запись {} не найдена", entryId);
            throw new AppException("Запись не найдена");
        }
    }
}