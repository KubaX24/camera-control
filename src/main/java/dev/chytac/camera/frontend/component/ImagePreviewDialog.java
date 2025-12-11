package dev.chytac.camera.frontend.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;
import dev.chytac.camera.service.ShareService;

import java.io.File;

public class ImagePreviewDialog extends Dialog {

    public ImagePreviewDialog(ShareService shareService, File image) {
        Button close = new Button(VaadinIcon.CLOSE.create(), e -> close());
        close.getStyle().set("margin-left", "auto");
        getHeader().add(new H2("Preview"), close);

        Image imagePreview = new Image();
        imagePreview.setAlt(image.getName());
        imagePreview.setId("dialog-image-preview");

        if (image.exists())
            imagePreview.setSrc(DownloadHandler.forFile(image));

        Anchor downloadImage = new Anchor();
        downloadImage.setHref(DownloadHandler.forFile(image));
        downloadImage.add(new Button("Download", VaadinIcon.DOWNLOAD.create()));

        Button shareImage = new Button("Share", VaadinIcon.SHARE.create());
        shareImage.addClickListener(e -> {
           shareService.uploadImage(image).ifPresentOrElse(s -> {
               ShareDialog shareDialog = new ShareDialog(s);
               shareDialog.open();
           }, () -> {
               Notification.show("Share - An error occurred while creating a link to the image!");
           });
        });

        Div dialogContent = new Div(new HorizontalLayout(downloadImage, shareImage), imagePreview);
        dialogContent.setId("dialog-content");

        add(dialogContent);
    }
}
