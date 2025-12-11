package dev.chytac.camera.frontend.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;

public class ShareDialog extends Dialog {

    public ShareDialog(String url) {
        Button close = new Button(VaadinIcon.CLOSE.create(), e -> close());
        close.getStyle().set("margin-left", "auto");
        getHeader().add(new H2("Share"), close);

        setWidth("500px");

        TextField urlField = new TextField();
        urlField.setValue(url);
        urlField.setWidthFull();
        urlField.setReadOnly(true);
        add(urlField);
    }
}
