package com.tagmarshal.golf.eventbus;

public class ShowScoreEvent {
    private final boolean active;

    public ShowScoreEvent(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
