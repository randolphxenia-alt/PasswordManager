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

/**
 * Бизнес-логика записей менеджера паролей: проверка ввода, защита пароля
 * перед сохранением, расшифровка, проверка хеш-паролей, поиск и сортировка (StreamAPI).
 */
public class PasswordEntryService {

    /** Логгер класса. */
    private static final Logger log = LogManager.getLogger(PasswordEntryService.class);

    /** Порядок записей: по названию без учёта регистра, затем по id. */
    private static final Comparator<DecryptedEntry> DISPLAY_ORDER =
            Comparator.comparing((DecryptedEntry entry) -> entry.data().title(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(DecryptedEntry::id);

    /** DAO записей. */
    private final PasswordEntryDAO entryDAO;

    /** Создаёт сервис с DAO по умолчанию. */
    public PasswordEntryService() {
        this(new PasswordEntryDAO());
    }

    /**
     * Создаёт сервис с указанным DAO (используется в тестах).
     *
     * @param entryDAO DAO записей
     */
    public PasswordEntryService(PasswordEntryDAO entryDAO) {
        this.entryDAO = entryDAO;
    }

    /**
     * Возвращает записи пользователя, отфильтрованные по запросу.
     * Поиск ведётся по названию, сайту и логину без учёта регистра.
     *
     * @param userId идентификатор пользователя
     * @param query  строка поиска (пустая или {@code null} — все записи)
     * @return отсортированный список записей
     * @throws AppException если БД недоступна
     */
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

    /**
     * Добавляет новую запись.
     *
     * @param userId идентификатор пользователя
     * @param data   данные записи в открытом виде
     * @throws AppException если данные некорректны или БД недоступна
     */
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

    /**
     * Изменяет запись. Пустой пароль означает «оставить текущий»;
     * при смене способа защиты текущий пароль перешифровывается, если он обратим.
     *
     * @param userId  идентификатор пользователя
     * @param entryId идентификатор записи
     * @param data    новые данные в открытом виде
     * @throws AppException если данные некорректны, запись не найдена или БД недоступна
     */
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

    /**
     * Удаляет запись.
     *
     * @param userId  идентификатор пользователя
     * @param entryId идентификатор записи
     * @throws AppException если запись не найдена или БД недоступна
     */
    public void deleteEntry(int userId, int entryId) throws AppException {
        try {
            requireFound(entryDAO.delete(userId, entryId), entryId);
            log.info("Пользователь {} удалил запись {}", userId, entryId);
        } catch (SQLException e) {
            log.error("Ошибка удаления записи {}", entryId, e);
            throw new AppException("Не удалось удалить запись", e);
        }
    }

    /**
     * Проверяет, совпадает ли введённый пароль с сохранённым (подходит для любого способа защиты,
     * в том числе для необратимых хешей).
     *
     * @param userId    идентификатор пользователя
     * @param entryId   идентификатор записи
     * @param candidate проверяемый пароль
     * @return {@code true}, если пароли совпадают
     * @throws AppException если пароль не введён, запись не найдена или данные повреждены
     */
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

    /**
     * Загружает запись пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param entryId идентификатор записи
     * @return запись
     * @throws AppException если запись не найдена
     * @throws SQLException при ошибке БД
     */
    private PasswordEntry loadEntry(int userId, int entryId) throws AppException, SQLException {
        return entryDAO.findById(userId, entryId).orElseThrow(() -> new AppException("Запись не найдена"));
    }

    /**
     * Определяет защищённый пароль для изменённой записи.
     *
     * @param current текущая запись из БД
     * @param data    новые данные
     * @return защищённый пароль для сохранения
     * @throws AppException если нужно сменить способ защиты необратимого пароля, а новый пароль не введён
     */
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

    /**
     * Подготавливает запись для отображения: расшифровывает пароль, если это возможно.
     * Повреждённая запись помечается, но не мешает показать остальные.
     *
     * @param entry запись из БД
     * @return запись для интерфейса
     */
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

    /**
     * Собирает запись для интерфейса.
     *
     * @param entry    запись из БД
     * @param password пароль в открытом виде (или пустая строка)
     * @param state    состояние пароля
     * @return запись для интерфейса
     */
    private static DecryptedEntry toDecrypted(PasswordEntry entry, String password, PasswordState state) {
        EntryData data = new EntryData(entry.title(), entry.website(), entry.login(), password, entry.storageMode());
        return new DecryptedEntry(entry.id(), data, state);
    }

    /**
     * Проверяет, подходит ли запись под строку поиска.
     *
     * @param entry  запись
     * @param needle строка поиска в нижнем регистре
     * @return {@code true}, если строка пустая или найдена в названии, сайте или логине
     */
    private static boolean matchesQuery(PasswordEntry entry, String needle) {
        return needle.isEmpty() || Stream.of(entry.title(), entry.website(), entry.login())
                .anyMatch(text -> text != null && text.toLowerCase(Locale.ROOT).contains(needle));
    }

    /**
     * Убирает пробелы по краям у текстовых полей (пароль не изменяется).
     *
     * @param data исходные данные
     * @return нормализованные данные
     */
    private static EntryData normalize(EntryData data) {
        return new EntryData(
                InputValidator.normalize(data.title()),
                InputValidator.normalize(data.website()),
                InputValidator.normalize(data.login()),
                data.password() == null ? "" : data.password(),
                data.storageMode());
    }

    /**
     * Собирает запись в формате хранения.
     *
     * @param entryId      идентификатор записи (0 для новой)
     * @param userId       идентификатор пользователя
     * @param data         данные в открытом виде
     * @param passwordData защищённый пароль
     * @return запись для БД
     */
    private static PasswordEntry toStored(int entryId, int userId, EntryData data, String passwordData) {
        return new PasswordEntry(entryId, userId, data.title(), data.website(), data.login(),
                passwordData, data.storageMode());
    }

    /**
     * Проверяет, что операция затронула запись.
     *
     * @param found   результат операции DAO
     * @param entryId идентификатор записи
     * @throws AppException если запись не найдена
     */
    private static void requireFound(boolean found, int entryId) throws AppException {
        if (!found) {
            log.warn("Запись {} не найдена", entryId);
            throw new AppException("Запись не найдена");
        }
    }
}