package dev.chytac.camera.service;

import dev.chytac.camera.entity.CameraConfig;
import dev.chytac.camera.entity.CameraFile;
import dev.chytac.camera.entity.CameraInformation;
import dev.chytac.camera.exception.CameraNotFoundException;
import dev.chytac.camera.exception.UnknownCommandOutput;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GphotoService {

    private static final String BASE_COMMAND = "gphoto2";
    private static final String PICTURE_PATTERN = "images/image-%y%m%d%H%M%S.jpg";

    public GphotoService() {
        File folder = new File("images");
        if (!folder.exists())
            folder.mkdir();
    }

    /**
     *
     * @return File name
     */
    public String captureAndDownload(int delay) {
        try {
            Thread.sleep(delay * 1000L);

            return captureAndDownload();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return File name
     */
    public String captureAndDownload() {
        try (BufferedReader br = executeCommand("--capture-image-and-download --filename \"" + PICTURE_PATTERN + "\"")) {
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Saving file as ")) {
                    return line.replace("Saving file as ", "");
                }

                output.append(line);
            }

            throw new UnknownCommandOutput(output.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return HashMap of folders names and list of {@link CameraFile}
     */
    public HashMap<String, List<CameraFile>> listOfFiles() {
        try (BufferedReader br = executeCommand("--list-files")) {
            String line;

            HashMap<String, List<CameraFile>> folders = new HashMap<>();

            String folderName = "none";
            List<CameraFile> files = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.startsWith("There are ")) {
                    folders.put(folderName, files);

                    files = new ArrayList<>();
                    folderName = line.split(" folder ")[1].replace("'", "").replace(".", "").trim();
                } else if (line.startsWith("#")) {
                    String[] fileInformation = line
                            .replace("#", "")
                            .replaceAll("\\s+", " ")
                            .split(" ");

                    CameraFile cameraFile = new CameraFile();
                    cameraFile.setId(Integer.parseInt(fileInformation[0]));
                    cameraFile.setName(fileInformation[1]);
                    cameraFile.setType(fileInformation[2]);
                    if (fileInformation[3].isEmpty()){
                        cameraFile.setSize(0);
                    } else {
                        cameraFile.setSize(Integer.parseInt(fileInformation[3]));
                    }
                    cameraFile.setFormat(fileInformation[5]);

                    files.add(cameraFile);
                }
            }

            return folders;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    @Async("cameraExecutor")
    public CompletableFuture<String> downloadThumbnail(CameraFile cameraFile) {
        String potentialFileName = "thumb_" + cameraFile.getName().replace("JPG", "jpg");

        File thumbFile = new File(potentialFileName);
        if (thumbFile.exists())
            return CompletableFuture.completedFuture(potentialFileName);

        try (BufferedReader br = executeCommand("--get-thumbnail=" + cameraFile.getId())) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Saving file as "))
                    return CompletableFuture.completedFuture(line.replace("Saving file as ", ""));
            }
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param cameraFile
     * @return file name
     */
    public String downloadFullResPicture(CameraFile cameraFile) {
        try (BufferedReader br = executeCommand("--get-file=" + cameraFile.getId())){
            StringBuilder output = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Saving file as "))
                    return line.replace("Saving file as ", "");

                output.append(line);
            }

            throw new UnknownCommandOutput(output.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CameraConfig> getConfigValues(String configValue) {
        try (BufferedReader br = executeCommand("--get-config " + configValue)){
            List<CameraConfig> cameraConfigs = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Choice: ")){
                    line = line.replace("Choice: ", "");
                    String[] config = line.split(" ");

                    if (config.length == 1) continue;

                    CameraConfig cameraConfig = new CameraConfig();
                    cameraConfig.setIndex(Integer.parseInt(config[0]));
                    cameraConfig.setValue(String.join(" ", java.util.Arrays.copyOfRange(config, 1, config.length)));

                    cameraConfigs.add(cameraConfig);
                } else if (line.startsWith("Current: ")) {
                    CameraConfig cameraConfig = new CameraConfig();
                    cameraConfig.setIndex(-1);
                    cameraConfig.setValue(line.replace("Current: ", ""));

                    cameraConfigs.add(cameraConfig);
                }
            }

            return cameraConfigs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConfigValue(String configValue, CameraConfig config) {
        try {
            executeCommandWithoutOutput("--set-config-index " + configValue + "=" + config.getIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CameraInformation getCameraInformation() throws CameraNotFoundException {
        try (BufferedReader br = executeCommand("--summary")){
            String line;

            CameraInformation cameraInformation = new CameraInformation();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Model: ")) {
                    cameraInformation.setModel(line.replace("Model: ", ""));
                } else if (line.startsWith("Manufacturer: ")) {
                    cameraInformation.setManufacturer(line.replace("Manufacturer: ", ""));
                }
            }

            if (cameraInformation.getModel() == null)
                throw new CameraNotFoundException();

            return cameraInformation;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedReader executeCommand(String args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("/bin/sh", "-c", BASE_COMMAND + " " + args);

        Process process = builder.start();

        InputStreamReader isr = new InputStreamReader(process.getInputStream());
        return new BufferedReader(isr);
    }


    private void executeCommandWithoutOutput(String args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("/bin/sh", "-c", BASE_COMMAND + " " + args);
        builder.start();
    }
}
