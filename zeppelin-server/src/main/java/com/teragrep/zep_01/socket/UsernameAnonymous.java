package com.teragrep.zep_01.socket;

public final class UsernameAnonymous implements Username {

    @Override
    public String asString() {
        return "anonymous";
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
