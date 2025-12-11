package dev.chytac.camera.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraFile {

    Integer id;
    String name;
    String type;
    Integer size;
    String format;

    public double getSizeInMb() {
        return size / 1000.0;
    }
}
