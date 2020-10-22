package br.ufc.projetofinal.Model;

public class TextMessage extends Message {
    private String message;

    public TextMessage(String sender, String message) {
        super(sender);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
