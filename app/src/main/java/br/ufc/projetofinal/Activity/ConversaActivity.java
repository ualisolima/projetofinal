package br.ufc.projetofinal.Activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import br.ufc.projetofinal.Adapter.ConversaAdapter;
import br.ufc.projetofinal.Adapter.ContatoAdapter;
import br.ufc.projetofinal.Model.ContatoCell;
import br.ufc.projetofinal.Service.DBHelper;
import br.ufc.projetofinal.Model.ContatoConversaCell;
import br.ufc.projetofinal.R;
import br.ufc.projetofinal.Service.SincronizarMensagensService;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversaActivity extends AppCompatActivity {

    private static ConversaAdapter conversaAdapter;
    private static ContatoAdapter contatoAdapter;
    public static ArrayList<String> nomes = new ArrayList<>();
    public static ArrayList<String> telefones = new ArrayList<>();
    private static Context applicationContext;
    private static StorageReference storageRef;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);

        applicationContext = getApplicationContext();
        carregaContatosDoTelefone();
        storageRef = FirebaseStorage.getInstance().getReference().child("profilePictures");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        EditText buscar = findViewById(R.id.buscar);
        buscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(tabLayout.getSelectedTabPosition() == 0) {
                    conversaAdapter.getFilter().filter(s.toString());
                }
                else {
                    contatoAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.barra_buscar);
        if(linearLayout.getVisibility() == View.GONE)
            finishAffinity();
        else
            resetSearch(linearLayout);
    }

    public void backPressed(View view) {
        LinearLayout linearLayout = findViewById(R.id.barra_buscar);
        resetSearch(linearLayout);
    }

    public void resetSearch(LinearLayout linearLayout) {
        linearLayout.setVisibility(View.GONE);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        EditText text = findViewById(R.id.buscar);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setVisibility(View.VISIBLE);
        text.setText("");
        if(tabLayout.getSelectedTabPosition() == 0) {
            conversaAdapter.getFilter().filter("");
        }
        else {
            contatoAdapter.getFilter().filter("");
        }
    }

    private void carregaContatosDoTelefone() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor telefones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

            while (telefones.moveToNext()) {
                String telefone = telefones.getString(telefones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+","");
                String nome = telefones.getString(telefones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if(!ConversaActivity.telefones.contains(telefone)) {
                    ConversaActivity.nomes.add(nome);
                    ConversaActivity.telefones.add(telefone);
                }
            }
            telefones.close();
        }
        cursor.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conversa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.hone_configuracoes) {
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            return true;
        }
        else if(id == R.id.home_buscar) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.barra_buscar);
            linearLayout.setVisibility(View.VISIBLE);
            TabLayout tabs = findViewById(R.id.tabs);
            tabs.setVisibility(View.GONE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        private static View conversas;
        private static View contatos;
        private static final String ARG_SECTION_NUMBER = "section_number";

        private BroadcastReceiver mensagemReceptor = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                intent.getSerializableExtra("message");
                criarListaMensagens();
            }
        };

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume() {
            super.onResume();
            criarListaMensagens();
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mensagemReceptor, new IntentFilter("serviceMessage"));
        }

        @Override
        public void onPause() {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mensagemReceptor);
            super.onPause();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if(getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                conversas = inflater.inflate(R.layout.fragment_conversa, container, false);
                FloatingActionButton fab = conversas.findViewById(R.id.nova_conversa);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ContatoActivity.class);
                        startActivity(intent);
                    }
                });
                criarListaMensagens();
                return conversas;
            }
            else {
                contatos = inflater.inflate(R.layout.fragment_contato, container, false);
                criarListaContatos();
                sincronizarContatosComFirebase();
                FloatingActionButton fab = (FloatingActionButton) contatos.findViewById(R.id.novo_contato);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                        startActivity(intent);
                    }
                });
                return contatos;
            }
        }
        //Cria a lista de usuários + ultima mensagem enviada para aparecer na aba de conversas

        private void criarListaMensagens() {
            ListView listaConversas = conversas.findViewById(R.id.lista);
            DBHelper helper = new DBHelper(getContext());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Message ORDER BY Date DESC", null);
            ArrayList<ContatoConversaCell> cells = new ArrayList<>();
            ContextWrapper wrapper = new ContextWrapper(getContext());
            File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
            int i;
            while (cursor.moveToNext()) {
                String friend = cursor.getString(cursor.getColumnIndex("Friend"));
                String name = "";
                if((i = ConversaActivity.telefones.indexOf(friend)) != -1) {
                    name = ConversaActivity.nomes.get(i);
                }
                else name = friend;
                String date = cursor.getString(cursor.getColumnIndex("Date"));
                String cDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if(cDate.equals(date.split(" ")[0])) {
                    String[] d = date.split(" ")[1].split(":");
                    date = d[0] + ":" + d[1];
                }
                else {
                    String[] d = date.split(" ")[0].split("-");
                    date = d[2] + "/" + d[1] + "/" + d[0];
                }
                String message = cursor.getString(cursor.getColumnIndex("Message"));
                if(message.equals("")) message = "Imagem";
                else if(message.contains("http://maps.google.com/maps/api/staticmap?center=")) message = "Localizacao";
                File file1 = new File(file, friend + ".jpg");
                if(file1.exists()) {
                    cells.add(new ContatoConversaCell(Uri.parse(file1.getAbsolutePath()), name, message, date, friend));
                }
                else {
                    cells.add(new ContatoConversaCell(null, name, message, date, friend));
                }

                conversaAdapter = new ConversaAdapter(getContext(), cells);
                listaConversas.setAdapter(conversaAdapter);
            }
        }

        //Cria a lista de contatos para aparecer na aba de contatos
        private void criarListaContatos() {
            ListView listaContatos = contatos.findViewById(R.id.lista);
            DBHelper helper = new DBHelper(getContext());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Friend", null);
            ArrayList<ContatoCell> cells = new ArrayList<>();
            ContextWrapper wrapper = new ContextWrapper(applicationContext);
            File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
            int i;
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex("Number"));
                if((i = telefones.indexOf(number)) != -1) {
                    File file1 = new File(file, number + ".jpg");
                    String status = cursor.getString(cursor.getColumnIndex("Status"));
                    String name = nomes.get(i);
                    if(file1.exists())
                        cells.add(new ContatoCell(Uri.parse(file1.getAbsolutePath()), name, status, number));
                    else
                        cells.add(new ContatoCell(null, name, status, number));
                }
            }
            contatoAdapter = new ContatoAdapter(getContext(), cells);
            listaContatos.setAdapter(contatoAdapter);
        }

        //sincroniza contatos com o firebase
        private void sincronizarContatosComFirebase() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DBHelper dbHelper = new DBHelper(getContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.execSQL("DELETE FROM Friend");
                    ContextWrapper wrapper = new ContextWrapper(applicationContext);
                    File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String number = ds.getKey();
                        String status = ds.child("status").getValue(String.class);

                        ContentValues values = new ContentValues();
                        values.put("Number", number);
                        values.put("Status", status);
                        db.insertWithOnConflict("Friend", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                        if(!number.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()) && telefones.contains(number)) {
                            final StorageReference reference = storageRef.child(number + ".jpg");
                            final File file1 = new File(file, number + ".jpg");
                            if(file1.exists()) {
                                final long localSize = file1.length();
                                reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        if(localSize != storageMetadata.getSizeBytes()) {
                                            reference.getFile(file1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else {
                                reference.getFile(file1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                    criarListaContatos();
                    getContext().startService(new Intent(getContext(), SincronizarMensagensService.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // mostrar 3 páginas
            return 2;
        }
    }

    public static class ViewHolder {
        public CircleImageView fotoPerfil;
        public LinearLayout layout;
        public TextView nomeTextView;
        public TextView ultimaMensagemTextView;
        public TextView dataTextView;
    }
}
