package com.passwordmanager.model;
public record DecryptedEntry(int id, EntryData data, PasswordState passwordState) {
}