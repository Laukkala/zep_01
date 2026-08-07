package com.teragrep.zep_01.notebook.repo;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public final class Revision {
/**
 * Represents the 'Revision' a point in life of the notebook
 */
    public static final Revision EMPTY = new Revision(StringUtils.EMPTY, StringUtils.EMPTY, 0);

    public final String id;
    public final String message;
    public final int time;

    public Revision(final String revId, final String message, final int time) {
        this.id = revId;
        this.message = message;
        this.time = time;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Revision revision = (Revision) o;
        return time == revision.time && Objects.equals(id, revision.id) && Objects.equals(message, revision.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, time);
    }
}
