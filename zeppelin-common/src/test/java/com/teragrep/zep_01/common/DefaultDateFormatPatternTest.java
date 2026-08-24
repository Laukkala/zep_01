package com.teragrep.zep_01.common;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Pattern;

class DefaultDateFormatPatternTest {

    @Test
    void testFormat() {
        final DefaultDateFormatPattern pattern = new DefaultDateFormatPattern();
        final Date date = Date.from(Instant.ofEpochSecond(1000000000L));
        final String formatted = pattern.format(date);

        // Formatted time should always contain 24 characters in format yyyy-mm-ddThh:mm:ss+timezone
        final Pattern regexPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{4}");
        Assertions.assertTrue(regexPattern.matcher(formatted).matches());
    }
}