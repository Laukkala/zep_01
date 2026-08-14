package com.teragrep.zep_01.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class DefaultDateFormatPattern {
    private final SimpleDateFormat format;

    public DefaultDateFormatPattern(){
        this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    }
    public String format(final Date date){
        return format.format(date);
    };

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DefaultDateFormatPattern that = (DefaultDateFormatPattern) o;
        return Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format);
    }
}
