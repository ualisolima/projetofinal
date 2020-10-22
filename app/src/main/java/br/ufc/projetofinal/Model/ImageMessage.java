package br.ufc.projetofinal.Model;

import android.net.Uri;

public class ImageMessage extends Message {
    private Uri imageUri;

    public ImageMessage(String sender, Uri imageUri) {
        super(sender);
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
