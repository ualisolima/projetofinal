package br.ufc.projetofinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

import br.ufc.projetofinal.Model.ImageMessage;
import br.ufc.projetofinal.Model.Message;
import br.ufc.projetofinal.R;
import br.ufc.projetofinal.Activity.RegistrarNumeroDeTelefoneActivity;
import br.ufc.projetofinal.Activity.MostrarImagemActivity;
import br.ufc.projetofinal.Model.TextMessage;

public class MensagemAdapter extends ArrayAdapter<Message> implements Filterable {
    private Context context;
    private ArrayList<Message> mensagens;
    private ArrayList<Message> MensagensFiltradas;
    private Filter filtro;

    public MensagemAdapter(Context context, ArrayList<Message> mensagens) {
        super(context, 0, mensagens);
        this.context = context;
        this.mensagens = mensagens;
        this.MensagensFiltradas = mensagens;
    }

    @Nullable
    @Override
    public Message getItem(int position) {
        return MensagensFiltradas.get(position);
    }

    @Override
    public int getCount() {
        return MensagensFiltradas.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Message message = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(message instanceof TextMessage) {
            TextMessage mensagemTexto = (TextMessage) message;
            if(mensagemTexto.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                convertView = inflater.inflate(R.layout.texto_enviado, null);
                TextView textoEnviado = convertView.findViewById(R.id.texto_enviado);
                textoEnviado.setText(mensagemTexto.getMessage());
                if(mensagemTexto.getMessage().contains("http://maps.google.com/maps/api/staticmap?center=")) {
                    textoEnviado.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            else {
                convertView = inflater.inflate(R.layout.texto_recebido, null);
                TextView textoRecebido = convertView.findViewById(R.id.texto_recebido);
                textoRecebido.setText(mensagemTexto.getMessage());
                if(mensagemTexto.getMessage().contains("http://maps.google.com/maps/api/staticmap?center=")) {
                    textoRecebido.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }
        else {
            ImageMessage mensagemFoto = (ImageMessage) message;
            if(mensagemFoto.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                convertView = inflater.inflate(R.layout.imagem_enviada, null);
                ImageView imagemEnviada = (ImageView) convertView.findViewById(R.id.imagem_enviada);
                try {
                    imagemEnviada.setTag(mensagemFoto.getImageUri().toString());
                    Bitmap bitmap = RegistrarNumeroDeTelefoneActivity.HelperClass.decodeBitmapFromFile(new File(mensagemFoto.getImageUri().toString()), 150, 150);
                    imagemEnviada.setImageBitmap(bitmap);
                }
                catch (Exception ex) {}
                imagemEnviada.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String) v.getTag();
                        if(tag != null && new File(tag).exists()) {
                            Intent intent = new Intent(getContext(), MostrarImagemActivity.class);
                            intent.putExtra("image", tag);
                            getContext().startActivity(intent);
                        }
                    }
                });
            }
            else {
                convertView = inflater.inflate(R.layout.imagem_recebida, null);
                ImageView imagemRecebida = convertView.findViewById(R.id.imagem_recebida);
                try {
                    imagemRecebida.setTag(mensagemFoto.getImageUri().toString());
                    Bitmap bitmap = RegistrarNumeroDeTelefoneActivity.HelperClass.decodeBitmapFromFile(new File(mensagemFoto.getImageUri().toString()), 150, 150);
                    imagemRecebida.setImageBitmap(bitmap);
                }
                catch (Exception ex) {}
                imagemRecebida.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String) v.getTag();
                        if( tag != null && new File(tag).exists()) {
                            Intent intent = new Intent(getContext(), MostrarImagemActivity.class);
                            intent.putExtra("image", tag);
                            getContext().startActivity(intent);
                        }
                    }
                });
            }
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filtro == null) {
            filtro = new MensagemFiltro();
        }
        return filtro;
    }

    private class MensagemFiltro extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0) {
                ArrayList<Message> filteredList = new ArrayList<>();
                for(Message message : mensagens) {
                    if(message instanceof TextMessage && ((TextMessage) message).getMessage().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        if(!((TextMessage) message).getMessage().contains("http://maps.google.com/maps/api/staticmap?center="))
                            filteredList.add(message);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            else {
                results.count = mensagens.size();
                results.values = mensagens;
            }
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            MensagensFiltradas = (ArrayList<Message>) results.values;
            notifyDataSetChanged();
        }
    }
}
