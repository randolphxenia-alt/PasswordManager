package com.passwordmanager.model;

public record UserCredentials(int id, String username, String passwordHash) {

    public User toUser() {
        return new User(id, username);
    }
}