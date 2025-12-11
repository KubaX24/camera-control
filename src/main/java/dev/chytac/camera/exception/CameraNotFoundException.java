package dev.chytac.camera.exception;

public class CameraNotFoundException extends RuntimeException {

    public CameraNotFoundException() {
        super("Camera not found.");
    }
}
