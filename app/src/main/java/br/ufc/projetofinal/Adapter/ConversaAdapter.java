package br.ufc.projetofinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import br.ufc.projetofinal.Activity.ConversaActivity;
import br.ufc.projetofinal.Activity.MensagemActivity;
import br.ufc.projetofinal.Model.ContatoConversaCell;
import br.ufc.projetofinal.R;
import br.ufc.projetofinal.Activity.RegistrarNumeroDeTelefoneActivity;
import br.ufc.projetofinal.Activity.DetalhesDeUsuarioActivity;

public class ConversaAdapter extends ArrayAdapter<ContatoConversaCell> implements Filterable{
    private ArrayList<ContatoConversaCell> conversas;
    private ArrayList<ContatoConversaCell> conversasFiltradas;
    private LayoutInflater inflater;
    private Filter filtro;

    public ConversaAdapter(Context context, ArrayList<ContatoConversaCell> conversas) {
        super(context, 0, conversas);
        this.conversas = conversas;
        conversasFiltradas = conversas;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Nullable
    @Override
    public ContatoConversaCell getItem(int position) {
        return conversasFiltradas.get(position);
    }

    @Override
    public int getCount() {
        return conversasFiltradas.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ContatoConversaCell cell = getItem(position);
        ConversaActivity.ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contato_conversa_cell, null);
            holder = new ConversaActivity.ViewHolder();
            holder.fotoPerfil = convertView.findViewById(R.id.foto);
            holder.layout = convertView.findViewById(R.id.layout);
            holder.nomeTextView = convertView.findViewById(R.id.nome_pessoa);
            holder.ultimaMensagemTextView = convertView.findViewById(R.id.ultimamensagem);
            holder.dataTextView = convertView.findViewById(R.id.date);
            convertView.setTag(holder);
        }
        else {
            holder = (ConversaActivity.ViewHolder) convertView.getTag();
        }

        if(cell.getPhoto() == null) {
            holder.fotoPerfil.setImageResource(R.drawable.conta);
        }
        else {
            //holder.photoImageView.setImageURI(cell.getPhoto());
            Bitmap bitmap = RegistrarNumeroDeTelefoneActivity.HelperClass.decodeBitmapFromFile(new File(cell.getPhoto().toString()), 50, 50);
            holder.fotoPerfil.setImageBitmap(bitmap);
        }
        holder.nomeTextView.setText(cell.getName());
        holder.ultimaMensagemTextView.setText(cell.getLastMessage());
        holder.dataTextView.setText(cell.getDate());

        holder.fotoPerfil.setTag(cell);
        holder.layout.setTag(cell);
        holder.dataTextView.setTag(cell);

        holder.fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContatoConversaCell contatoConversaCell = (ContatoConversaCell) v.getTag();
                Intent intent = new Intent(getContext(), DetalhesDeUsuarioActivity.class);
                intent.putExtra("name", contatoConversaCell.getName());
                intent.putExtra("phone", contatoConversaCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContatoConversaCell contatoConversaCell = (ContatoConversaCell) v.getTag();
                Intent intent = new Intent(getContext(), MensagemActivity.class);
                intent.putExtra("name", contatoConversaCell.getName());
                intent.putExtra("phone", contatoConversaCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        holder.dataTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContatoConversaCell contatoConversaCell = (ContatoConversaCell) v.getTag();
                Intent intent = new Intent(getContext(), MensagemActivity.class);
                intent.putExtra("name", contatoConversaCell.getName());
                intent.putExtra("phone", contatoConversaCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filtro == null) {
            filtro = new ConversaFiltro();
        }
        return filtro;
    }

    private class ConversaFiltro extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0) {
                ArrayList<ContatoConversaCell> filteredList = new ArrayList<>();
                for(ContatoConversaCell cell : conversas) {
                    if(cell.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(cell);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            else {
                results.count = conversas.size();
                results.values = conversas;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            conversasFiltradas = (ArrayList<ContatoConversaCell>) results.values;
            notifyDataSetChanged();
        }
    }
}
