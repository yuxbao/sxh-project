package com.github.paicoding.forum.web.websocket;

import java.security.Principal;
import java.util.Objects;

public class WebSocketUserPrincipal implements Principal {
    private final String name;

    public WebSocketUserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebSocketUserPrincipal that = (WebSocketUserPrincipal) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
