package br.ufc.projetofinal.Model;

import android.net.Uri;

public class ContatoConversaCell {
    private Uri photo;
    private String name;
    private String lastMessage;
    private String date;
    private String phone;

    public ContatoConversaCell(Uri photo, String name, String lastMessage, String date, String phone) {
        this.photo = photo;
        this.name = name;
        this.lastMessage = lastMessage;
        this.date = date;
        this.phone = phone;
    }

    public Uri getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getDate() {
        return date;
    }

    public String getPhone() {
        return phone;
    }
}
