package com.teragrep.zep_01.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class DefaultDateFormatPattern {
    private final SimpleDateFormat format;
    private final Date date;

    public DefaultDateFormatPattern(Date date){
        this.date = date;
        this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    }
    public String format(){
        return format.format(date);
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultDateFormatPattern pattern = (DefaultDateFormatPattern) o;
        return Objects.equals(format, pattern.format) && Objects.equals(date, pattern.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, date);
    }
}
