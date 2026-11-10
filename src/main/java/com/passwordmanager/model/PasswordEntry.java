package com.passwordmanager.model;
public record PasswordEntry(int id, int userId, String title, String website,
                            String login, String passwordData, StorageMode storageMode) {
}