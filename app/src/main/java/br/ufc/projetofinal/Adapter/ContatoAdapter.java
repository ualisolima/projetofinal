package br.ufc.projetofinal.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import br.ufc.projetofinal.Model.ContatoCell;
import br.ufc.projetofinal.R;
import br.ufc.projetofinal.Activity.RegistrarNumeroDeTelefoneActivity;
import br.ufc.projetofinal.Activity.DetalhesDeUsuarioActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContatoAdapter extends ArrayAdapter<ContatoCell> {
    private ArrayList<ContatoCell> contatos;
    private ArrayList<ContatoCell> contatosFiltrados;
    private LayoutInflater inflater;
    private Filter filtro;

    public ContatoAdapter(Context context, ArrayList<ContatoCell> contatos) {
        super(context, 0, contatos);
        this.contatos = contatos;
        this.contatosFiltrados = contatos;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Nullable
    @Override
    public ContatoCell getItem(int position) {
        return contatosFiltrados.get(position);
    }

    @Override
    public int getCount() {
        return contatosFiltrados.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ContatoCell cell = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contato_cell, null);
        }

        CircleImageView fotoPerfil = convertView.findViewById(R.id.foto);
        LinearLayout layout = convertView.findViewById(R.id.layout);
        TextView nomeTextView = convertView.findViewById(R.id.nome_pessoa);
        TextView statusTextView = convertView.findViewById(R.id.status);

        if(cell.getPhoto() == null) {
            fotoPerfil.setImageResource(R.drawable.conta);
        } else {
            Bitmap bitmap = RegistrarNumeroDeTelefoneActivity.HelperClass.decodeBitmapFromFile(new File(cell.getPhoto().toString()), 100, 100);
            fotoPerfil.setImageBitmap(bitmap);
        }
        nomeTextView.setText(cell.getName());
        statusTextView.setText(cell.getStatus());

        layout.setTag(cell);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContatoCell contatoCell = (ContatoCell) v.getTag();
                Intent intent = new Intent(getContext(), DetalhesDeUsuarioActivity.class);
                intent.putExtra("name", contatoCell.getName());
                intent.putExtra("phone", contatoCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filtro == null) {
            filtro = new ContatoFiltro();
        }
        return filtro;
    }

    private class ContatoFiltro extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0) {
                ArrayList<ContatoCell> filteredList = new ArrayList<>();
                for(ContatoCell cell : contatos) {
                    if(cell.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(cell);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            else {
                results.count = contatos.size();
                results.values = contatos;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contatosFiltrados = (ArrayList<ContatoCell>) results.values;
            notifyDataSetChanged();
        }
    }
}