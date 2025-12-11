package dev.chytac.camera.frontend.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import dev.chytac.camera.entity.CameraFile;
import dev.chytac.camera.frontend.CameraAppLayout;
import dev.chytac.camera.frontend.component.ShareDialog;
import dev.chytac.camera.service.GphotoService;
import dev.chytac.camera.service.ShareService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route("/file")
public class FileView extends CameraAppLayout {

    private final Div fileLayout = new Div();

    private final GphotoService gphotoService;
    private final ShareService shareService;

    public FileView(GphotoService gphotoService, ShareService shareService) {
        super(gphotoService);

        this.gphotoService = gphotoService;
        this.shareService = shareService;

        fileLayout.setId("file-layout");

        HashMap<String, List<CameraFile>> files = gphotoService.listOfFiles();

        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout folderLayout = new HorizontalLayout();
        folderLayout.setId("folder-layout");

        files.forEach((folder, filesList) -> {
            if(folder.equals("none")) return;

            Button folderButton = new Button(folder, VaadinIcon.FOLDER.create());

            folderButton.addClickListener(e -> {
                fileLayout.removeAll();

                UI ui = e.getSource().getUI().orElseThrow();

                for (CameraFile cameraFile : filesList) {
                    fileLayout.add(imagePreview(cameraFile, ui));
                }
            });

            folderLayout.add(folderButton);
        });

        layout.add(folderLayout, fileLayout);

        setContent(layout);
    }

    private Div imagePreview(CameraFile cameraFile, UI ui) {
        Div div = new Div();
        div.addClassName("image-preview");

        Image image = new Image();

        CompletableFuture<String> future = gphotoService.downloadThumbnail(cameraFile);

        future.thenAccept(result -> {
            if (result != null) {
                ui.access(() -> {
                    File imageFile = new File(result);
                    if (imageFile.exists())
                        image.setSrc(DownloadHandler.forFile(imageFile));
                });
            }
        });

        Anchor downloadButton = new Anchor();
        downloadButton.add(new Button(VaadinIcon.DOWNLOAD.create()));

        downloadButton.setHref(downloadEvent -> {
            String fileName = gphotoService.downloadFullResPicture(cameraFile);
            File fullResImage = new File(fileName);

            downloadEvent.setContentType(cameraFile.getFormat());
            downloadEvent.setFileName(fullResImage.getName());

            try (OutputStream outputStream = downloadEvent.getOutputStream()) {
                Files.copy(fullResImage.toPath(), outputStream);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to download file", ex);
            }

            fullResImage.delete();
        });

        Button shareButton = new Button(VaadinIcon.SHARE.create());
        shareButton.addClickListener(buttonClickEvent -> {
            String fileName = gphotoService.downloadFullResPicture(cameraFile);
            File fullResImage = new File(fileName);

            shareService.uploadImage(fullResImage).ifPresentOrElse(s -> {
                ShareDialog shareDialog = new ShareDialog(s);
                shareDialog.open();
            }, () -> {
                Notification.show("Share - An error occurred while creating a link to the image!");
            });

            fullResImage.delete();
        });

        shareButton.addClassName("share-button");

        Div imageInfo = new Div();
        imageInfo.addClassName("image-preview-info");
        Span size = new Span(String.format("%.2f", cameraFile.getSizeInMb()) + " MB");
        size.addClassName("image-preview-size");
        imageInfo.add(new Span(cameraFile.getName()), size, shareButton, downloadButton);

        div.add(image, imageInfo);

        return div;
    }
}
