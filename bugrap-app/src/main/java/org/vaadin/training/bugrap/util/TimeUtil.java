/**
 * 
 */
package org.vaadin.training.bugrap.util;

import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author Tulio Garcia
 *
 */
public class TimeUtil {

    /**
     * 
     * @param locale Locale to use.
     * @return Date formatter.
     */
    public static Function<Date, String> createTimeElapsedDateFormatter(Locale locale) {
        PrettyTime prettyTime = new PrettyTime(locale);
        return date -> {
            if (date == null)
                return null;

            return prettyTime.format(date);

        };
    }
}
