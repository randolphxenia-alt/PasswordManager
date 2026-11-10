package com.passwordmanager.model;
public record EntryData(String title, String website, String login, String password,
                        StorageMode storageMode) {
}