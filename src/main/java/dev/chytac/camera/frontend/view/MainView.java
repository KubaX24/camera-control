package dev.chytac.camera.frontend.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import dev.chytac.camera.entity.ConfigValues;
import dev.chytac.camera.frontend.CameraAppLayout;
import dev.chytac.camera.frontend.component.CameraConfigComboBox;
import dev.chytac.camera.frontend.component.ImagePreviewDialog;
import dev.chytac.camera.service.GphotoService;
import dev.chytac.camera.service.ShareService;

import java.io.File;

@Route("/")
public class MainView extends CameraAppLayout {

    public MainView(GphotoService gphotoService, ShareService shareService) {
        super(gphotoService);

        ComboBox<Integer> delay = new ComboBox<>("Delay");
        CameraConfigComboBox shutterSpeed = new CameraConfigComboBox(gphotoService, ConfigValues.SHUTTER_SPEED);
        CameraConfigComboBox iso = new CameraConfigComboBox(gphotoService, ConfigValues.ISO);
        CameraConfigComboBox aperture = new CameraConfigComboBox(gphotoService, ConfigValues.APERTURE);
        CameraConfigComboBox whiteBalance = new CameraConfigComboBox(gphotoService, ConfigValues.WHITE_BALANCE);

        Button takePicture = new Button("Take picture");
        takePicture.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        delay.setItems(0, 1, 5, 10);
        delay.setValue(0);
        delay.setItemLabelGenerator(integer -> integer + "s");

        takePicture.addClickListener(e -> {
            String fileName;

            if (delay.getValue() == 0) {
                fileName = gphotoService.captureAndDownload();
            } else {
                fileName = gphotoService.captureAndDownload(delay.getValue());
            }

            File image = new File(fileName);

            ImagePreviewDialog imagePreviewDialog = new ImagePreviewDialog(shareService, image);
            imagePreviewDialog.open();
        });

        VerticalLayout layout = new VerticalLayout();

        Div cameraSettings = new Div(new H2("Camera settings"), new HorizontalLayout(delay, shutterSpeed, iso, aperture, whiteBalance));
        cameraSettings.setId("camera-settings");

        layout.add(cameraSettings, takePicture);
        setContent(layout);
    }
}
