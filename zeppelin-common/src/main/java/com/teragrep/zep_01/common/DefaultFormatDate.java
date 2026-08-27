package com.teragrep.zep_01.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class DefaultFormatDate {
    private final SimpleDateFormat format;
    private final Date date;

    public DefaultFormatDate(Date date){
        this(date, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    private DefaultFormatDate(Date date, SimpleDateFormat format){
        this.date = date;
        this.format = format;
    }
    public String asFormattedString(){
        return format.format(date);
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultFormatDate pattern = (DefaultFormatDate) o;
        return Objects.equals(format, pattern.format) && Objects.equals(date, pattern.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, date);
    }
}
