package org.vaadin.training.client;

import org.vaadin.training.MyComponent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.VLabel;
import com.vaadin.shared.ui.Connect;

/**
 * This a dummy client-side connector for MyComponent.
 */
@Connect(MyComponent.class)
public class MyComponentConnector extends AbstractComponentConnector {

    @Override
    public VLabel getWidget() {
        return (VLabel) super.getWidget();
    }

    @Override
    protected void init() {
        super.init();

        getWidget().setText("This is a dummy component.");
    }
}