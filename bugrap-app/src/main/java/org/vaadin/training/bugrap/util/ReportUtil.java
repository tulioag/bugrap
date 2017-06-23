/**
 * 
 */
package org.vaadin.training.bugrap.util;

import java.util.EnumMap;

import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Report.Priority;
import org.vaadin.bugrap.domain.entities.Report.Type;

/**
 * @author Tulio Garcia
 *
 */
public class ReportUtil {

    private static final char PRIORITY_REPR = '❚'; 

    private static EnumMap<Report.Type, String> descriptions = new EnumMap<>(
            Report.Type.class);
    static {
        for (Report.Type type : Report.Type.values()) {
            String name = type.name();
            String description = new StringBuilder().append(name.charAt(0))
                    .append(name.substring(1).toLowerCase()).toString();
            descriptions.put(type, description);
        }

    }

    public static String toString(Priority priority) {
        if (priority == null)
            return null;
        StringBuilder sb = new StringBuilder(priority.ordinal());
        for (int i = 0; i <= priority.ordinal(); i++)
            sb.append(PRIORITY_REPR);
        return sb.toString();

    }
    
    public static String toString(Type type) {
        return descriptions.get(type);
    }
}
