package dev.chytac.camera.frontend.component;

import com.vaadin.flow.component.combobox.ComboBox;
import dev.chytac.camera.entity.CameraConfig;
import dev.chytac.camera.entity.ConfigValues;
import dev.chytac.camera.service.GphotoService;

import java.util.List;

public class CameraConfigComboBox extends ComboBox<CameraConfig> {

    public CameraConfigComboBox(GphotoService gphotoService, ConfigValues configValue) {
        super(configValue.getLabel());

        List<CameraConfig> cameraConfigs = gphotoService.getConfigValues(configValue.getConfig());
        setItems(cameraConfigs.stream().filter(cameraConfig -> cameraConfig.getIndex() != -1).toList());
        setItemLabelGenerator(CameraConfig::getValue);

        CameraConfig current = cameraConfigs.stream().filter(cameraConfig -> cameraConfig.getIndex() == -1).findFirst().orElse(new CameraConfig(-1, "0"));

        setValue(cameraConfigs.stream()
                .filter(cameraConfig -> cameraConfig.getIndex() != -1 && cameraConfig.getValue().equals(current.getValue()))
                .findFirst()
                .orElse(null)
        );

        addValueChangeListener(e -> {
            if (e.getValue() == null) return;

            gphotoService.setConfigValue(configValue.getConfig(), e.getValue());
        });
    }
}
