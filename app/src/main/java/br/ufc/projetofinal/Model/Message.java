package br.ufc.projetofinal.Model;

import android.net.Uri;

public abstract class Message {
    private String date;
    private String sender;

    public Message(String sender) {
        this.sender = sender;
    }

    public String  getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

