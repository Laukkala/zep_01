package com.teragrep.zep_01.common;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class DefaultDateFormatPattern extends DateFormat {
    private final DateFormat origin;
    public DefaultDateFormatPattern(){
        this(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    public DefaultDateFormatPattern(SimpleDateFormat format){
        this.origin = format;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return origin.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return origin.parse(source, pos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultDateFormatPattern pattern = (DefaultDateFormatPattern) o;
        return Objects.equals(origin, pattern.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), origin);
    }
}
