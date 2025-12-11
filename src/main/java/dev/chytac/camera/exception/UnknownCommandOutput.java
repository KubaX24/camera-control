package dev.chytac.camera.exception;

public class UnknownCommandOutput extends RuntimeException
{
    public UnknownCommandOutput(String output) {
        super(output);
    }
}
