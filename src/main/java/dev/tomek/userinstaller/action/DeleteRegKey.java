package dev.tomek.userinstaller.action;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteRegKey implements Action {

    private final String key;

    @Override
    public String getName() {
        return "Removing registry key: " + key;
    }

    @Override
    public boolean perform() {
        return false;
    }
}
