package dev.chytac.camera.frontend;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import dev.chytac.camera.entity.CameraInformation;
import dev.chytac.camera.exception.CameraNotFoundException;
import dev.chytac.camera.frontend.view.FileView;
import dev.chytac.camera.frontend.view.MainView;
import dev.chytac.camera.service.GphotoService;

public class CameraAppLayout extends AppLayout {

    private boolean cameraFound = true;

    public CameraAppLayout(GphotoService gphotoService) {
        try {
            CameraInformation cameraInformation = gphotoService.getCameraInformation();
            addToNavbar(new H1(VaadinIcon.CAMERA.create(), new Span(cameraInformation.getModel())), navigation());

            cameraFound = true;
        } catch (CameraNotFoundException e) {
            cameraFound = false;
        }
    }

    @Override
    public void setContent(Component content) {
        if (!cameraFound)
            super.setContent(new H2(VaadinIcon.WARNING.create(), new Span("Camera not found. Check if the camera is connected and reload the page."), VaadinIcon.WARNING.create()));
        else
            super.setContent(content);
    }

    private HorizontalLayout navigation() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("navigation");

        Button studio = new Button("Studio", e -> getUI().ifPresent(ui -> ui.navigate(MainView.class)));
        Button files = new Button("Files", e -> getUI().ifPresent(ui -> ui.navigate(FileView.class)));

        studio.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        files.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(studio, files);
        return layout;
    }
}
