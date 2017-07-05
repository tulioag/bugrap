package org.vaadin.training.bugrap.reports;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

/**
 * 
 * Messages to the user available in this view.
 * 
 * @author Tulio Garcia
 *
 */
class Messages {

    void showValidationErrorsMessage() {
        Notification.show("There are validation errors",
                Notification.Type.ERROR_MESSAGE);
    }

    void showConcurrentModificationErrorMessage() {
        Notification.show("The report has been modified by another user",
                Notification.Type.ERROR_MESSAGE);
    }

    void showUpdateSucessfullMessage() {
        Notification notification = new Notification("Update successful",
                Notification.Type.TRAY_NOTIFICATION);
        notification.setDelayMsec(1000);
        notification.setStyleName("success");
        notification.show(Page.getCurrent());

    }
}