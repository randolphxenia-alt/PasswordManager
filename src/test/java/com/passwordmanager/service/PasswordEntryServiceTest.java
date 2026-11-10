package com.passwordmanager.service;

import com.passwordmanager.model.DecryptedEntry;
import com.passwordmanager.model.EntryData;
import com.passwordmanager.model.PasswordState;
import com.passwordmanager.model.StorageMode;
import com.passwordmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PasswordEntryServiceTest extends DatabaseTestBase {


    private static final String PASSWORD = "P@ssw0rd";


    private final PasswordEntryService entryService = new PasswordEntryService();


    private User owner;






    @BeforeEach
    void createOwner() throws AppException {
        AuthService authService = new AuthService();
        authService.register("owner", "secret1", "secret1");
        owner = authService.login("owner", "secret1");
    }







    @ParameterizedTest
    @EnumSource(value = StorageMode.class, names = {"BASE64", "FEISTEL"})
    @DisplayName("Обратимые способы: пароль восстанавливается")
    void reversibleEntry(StorageMode mode) throws AppException {
        DecryptedEntry entry = addAndLoad("GitHub", mode);
        assertEquals(PasswordState.AVAILABLE, entry.passwordState());
        assertEquals(PASSWORD, entry.data().password());
    }







    @ParameterizedTest
    @EnumSource(value = StorageMode.class, names = {"MD5", "SALTED_HASH"})
    @DisplayName("Хеш-способы: пароль скрыт, но проверяется")
    void hashedEntry(StorageMode mode) throws AppException {
        DecryptedEntry entry = addAndLoad("Steam", mode);
        assertEquals(PasswordState.HASHED, entry.passwordState());
        assertTrue(entryService.verifyPassword(owner.id(), entry.id(), PASSWORD));
        assertFalse(entryService.verifyPassword(owner.id(), entry.id(), "wrong"));
    }






    @Test
    @DisplayName("Поиск и сортировка (StreamAPI)")
    void searchAndSort() throws AppException {
        entryService.addEntry(owner.id(), new EntryData("Яндекс", "ya.ru", "nik", PASSWORD, StorageMode.BASE64));
        entryService.addEntry(owner.id(), new EntryData("github", "github.com", "dev", PASSWORD, StorageMode.MD5));
        entryService.addEntry(owner.id(), new EntryData("Bonch LK", "lk.sut.ru", "2613", PASSWORD, StorageMode.FEISTEL));

        assertEquals(List.of("Bonch LK", "github", "Яндекс"), titles(entryService.findEntries(owner.id(), null)));
        assertEquals(List.of("github"), titles(entryService.findEntries(owner.id(), "GIT")));
        assertEquals(List.of("Bonch LK"), titles(entryService.findEntries(owner.id(), "sut.ru")));
        assertEquals(List.of("Яндекс"), titles(entryService.findEntries(owner.id(), "nik")));
        assertTrue(entryService.findEntries(owner.id(), "нет такого").isEmpty());
    }






    @Test
    @DisplayName("Изменение: пустой пароль сохраняет прежний, смена способа перешифровывает")
    void updateKeepsAndReencryptsPassword() throws AppException {
        int id = addAndLoad("Old", StorageMode.BASE64).id();
        entryService.updateEntry(owner.id(), id, new EntryData("New", "", "login", "", StorageMode.FEISTEL));
        DecryptedEntry updated = entryService.findEntries(owner.id(), "").get(0);
        assertEquals("New", updated.data().title());
        assertEquals(StorageMode.FEISTEL, updated.data().storageMode());
        assertEquals(PASSWORD, updated.data().password());
    }






    @Test
    @DisplayName("Изменение: хеш-пароль нельзя перешифровать без ввода")
    void hashedPasswordNeedsNewValueToChangeMode() throws AppException {
        int id = addAndLoad("Hashed", StorageMode.MD5).id();
        assertThrows(AppException.class, () -> entryService.updateEntry(owner.id(), id,
                new EntryData("Hashed", "", "login", "", StorageMode.BASE64)));
    }






    @Test
    @DisplayName("Удаление записи; повторное удаление → ошибка")
    void delete() throws AppException {
        int id = addAndLoad("Temp", StorageMode.BASE64).id();
        entryService.deleteEntry(owner.id(), id);
        assertTrue(entryService.findEntries(owner.id(), "").isEmpty());
        assertThrows(AppException.class, () -> entryService.deleteEntry(owner.id(), id));
    }






    @Test
    @DisplayName("Чужие записи недоступны")
    void entriesAreIsolatedByUser() throws AppException {
        int id = addAndLoad("Private", StorageMode.FEISTEL).id();
        int strangerId = owner.id() + 1;
        assertTrue(entryService.findEntries(strangerId, "").isEmpty());
        assertThrows(AppException.class, () -> entryService.deleteEntry(strangerId, id));
        assertThrows(AppException.class, () -> entryService.verifyPassword(strangerId, id, PASSWORD));
    }


    @Test
    @DisplayName("Некорректная запись отклоняется")
    void invalidEntryRejected() {
        assertThrows(AppException.class, () -> entryService.addEntry(owner.id(),
                new EntryData("   ", "", "login", PASSWORD, StorageMode.FEISTEL)));
    }









    private DecryptedEntry addAndLoad(String title, StorageMode mode) throws AppException {
        entryService.addEntry(owner.id(), new EntryData(title, "site.ru", "login", PASSWORD, mode));
        return entryService.findEntries(owner.id(), title).get(0);
    }







    private static List<String> titles(List<DecryptedEntry> entries) {
        return entries.stream().map(entry -> entry.data().title()).toList();
    }
}