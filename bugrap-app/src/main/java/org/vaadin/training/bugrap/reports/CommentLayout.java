package org.vaadin.training.bugrap.reports;

import java.util.Date;
import java.util.function.Function;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Label;

/**
 * Layout of a single comment.
 * 
 * @author Tulio Garcia
 *
 */
@SuppressWarnings("serial")
class CommentLayout extends Label {
    private final Function<Date, String> timeFormatter;

    CommentLayout(Function<Date, String> timeFormatter, String autorName,
            Date date, String text) {
        this.timeFormatter = timeFormatter;
        setIcon(VaadinIcons.USER);
        setStyleName("comment-content");
        setCaption(formatCaption(autorName, date));
        setValue(text);
    }

    String formatCaption(String autorName, Date date) {
        String formattedDate = timeFormatter.apply(date);
        return String.format("%s (%s)", autorName, formattedDate);
    }
}