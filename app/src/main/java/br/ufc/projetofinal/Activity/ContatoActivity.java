package br.ufc.projetofinal.Activity;

import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

import br.ufc.projetofinal.Adapter.ContatoAdapter2;
import br.ufc.projetofinal.Model.ContatoCell;
import br.ufc.projetofinal.Service.DBHelper;
import br.ufc.projetofinal.R;

public class ContatoActivity extends AppCompatActivity {

    Cursor cursor;
    ContatoAdapter2 contatoAdapter2;
    ListView listaDeContatos;
    EditText contato_filtro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contato);

        setToolbar();

        listaDeContatos = findViewById(R.id.lista);
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM Friend", null);
        ArrayList<ContatoCell> cells = new ArrayList<>();
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        int i;
        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex("Number"));
            if((i = ConversaActivity.telefones.indexOf(number)) != -1) {
                File file1 = new File(file, number + ".jpg");
                String status = cursor.getString(cursor.getColumnIndex("Status"));
                String name = ConversaActivity.nomes.get(i);
                if(file1.exists())
                    cells.add(new ContatoCell(Uri.parse(file1.getAbsolutePath()), name, status, number));
                else
                    cells.add(new ContatoCell(null, name, status, number));
            }
        }
        contatoAdapter2 = new ContatoAdapter2(this, cells);
        listaDeContatos.setAdapter(contatoAdapter2);
        contato_filtro = findViewById(R.id.buscar);
        contato_filtro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contatoAdapter2.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


}
