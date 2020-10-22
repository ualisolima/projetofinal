package br.ufc.projetofinal.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.ufc.projetofinal.Service.DBHelper;
import br.ufc.projetofinal.Model.ImageMessage;
import br.ufc.projetofinal.Model.Message;
import br.ufc.projetofinal.Adapter.MensagemAdapter;
import br.ufc.projetofinal.R;
import br.ufc.projetofinal.Service.SincronizarMensagensService;
import br.ufc.projetofinal.Model.TextMessage;
import de.hdodenhof.circleimageview.CircleImageView;

public class MensagemActivity extends AppCompatActivity {

    private ListView listaMensagens;
    private LinearLayout attachmentGrid;
    private EditText mensagemTextView;
    public static final int CAMERA = 2;
    public static final int GALLERY = 3;

    private String telefone;
    private String nome;

    private DatabaseReference dbRef1;
    private DatabaseReference dbRef2;
    private StorageReference stgRef;

    private MensagemAdapter mensagemAdapter;

    private BroadcastReceiver mensagemReceptor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getSerializableExtra("message");
            mostrarMensagens();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensagem);

        Intent intent = getIntent();
        nome = (String) intent.getSerializableExtra("name");
        telefone = (String)intent.getSerializableExtra("phone");
        if(nome == null) {
            nome = telefone;
        }
        CircleImageView fotoContato = findViewById(R.id.fotoPerfil);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, telefone + ".jpg");
        if(file.exists()) {
            ((CircleImageView) findViewById(R.id.friend_image)).setImageURI(Uri.parse(file.getAbsolutePath()));
        }

        definirBarraFerramentas();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("messages");
        dbRef1 = mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + telefone);
        dbRef2 = mDatabase.child(telefone + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        stgRef = FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + telefone);

        listaMensagens = findViewById(R.id.mensagem_lista);
        mostrarMensagens();

        attachmentGrid = (LinearLayout) findViewById(R.id.attachment_grid);

        mensagemTextView = findViewById(R.id.mensagem_texto);
        mensagemTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachmentGrid.setVisibility(View.GONE);
            }
        });

        EditText buscar = findViewById(R.id.barra_buscar);
        buscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mensagemAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SincronizarMensagensService.conversaAberta = telefone;
        LocalBroadcastManager.getInstance(this).registerReceiver(mensagemReceptor, new IntentFilter("serviceMessage"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mensagemReceptor);
        SincronizarMensagensService.conversaAberta = "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SincronizarMensagensService.conversaAberta = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mensagem_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.mensagem_apagarconversa) {
            DBHelper helper = new DBHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM Message WHERE Friend='" + telefone + "'");
            db.execSQL("DELETE FROM MyMessage WHERE Friend='" + telefone + "'");
            dbRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.child("type").getRef().setValue(null);
                        ds.child("message").getRef().setValue(null);
                    }
                    mostrarMensagens();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
            File file = wrapper.getDir(telefone,MODE_PRIVATE);
            if (file.isDirectory())
            {
                String[] children = file.list();
                for (String child : children)
                {
                    new File(file, child).delete();
                }
            }
        }
        else if(id == R.id.mensagem_buscar) {
            findViewById(R.id.tool_bar_parent).setBackground(getResources().getDrawable(R.color.white));
            findViewById(R.id.tool_bar_child).setVisibility(View.GONE);
            findViewById(R.id.barra_buscar).setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void limparPesquisa() {
        findViewById(R.id.tool_bar_parent).setBackground(getResources().getDrawable(R.color.teal));
        findViewById(R.id.tool_bar_child).setVisibility(View.VISIBLE);
        EditText editText = (EditText) findViewById(R.id.barra_buscar);
        editText.setVisibility(View.GONE);
        editText.setText("");
        mensagemAdapter.getFilter().filter(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() != null) {
                if (requestCode == CAMERA) {
                    enviarImagem((Bitmap) data.getExtras().get("data"));
                }
                else if (requestCode == GALLERY) {
                    Uri imageUri = data.getData();
                    ImageView imageView = (ImageView) findViewById(R.id.image_view);
                    imageView.setImageURI(imageUri);
                    Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    enviarImagem(bitmap);
                }
            }
            else {
                Toast.makeText(this, "Sem conexao disponivel", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void enviarImagem(@NonNull Bitmap foto) {
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir(telefone,MODE_PRIVATE);
        Date date = new Date();
        final String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        final String n1 = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                File file1 = new File(file, n1 + ".jpg");

        try {
            OutputStream stream = null;
            stream = new FileOutputStream(file1);
            foto.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();

            StorageReference reference = stgRef.child(n1 + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            foto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(MensagemActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Map<String, String> map = new HashMap<>();
                    map.put("type", "image");
                    dbRef1.child(n).setValue(map);
                    dbRef2.child(n).setValue(map);
                }
            });
        }
        catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }





    public void mostrarMensagens() {
        dbRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Message> messages = new ArrayList<>();
                ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                File file = wrapper.getDir(telefone,MODE_PRIVATE);
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String[] key = ds.getKey().split(" ");
                    String sender = key[2];
                    String date = key[0] + " " + key[1];
                    String type = ds.child("type").getValue(String.class);
                    if(type.equals("text")) {
                        String message = ds.child("message").getValue(String.class);
                        TextMessage textMessage = new TextMessage(sender, message);
                        textMessage.setDate(date);
                        messages.add(textMessage);
                        System.out.println(sender + " " + date + " " + message);
                    }
                    else if(type.equals("image")) {
                        File file1 = new File(file, sender + " " + date + ".jpg");
                        ImageMessage imageMessage = null;
                        if(file1.exists()) {
                            imageMessage = new ImageMessage(sender, Uri.parse(file1.getAbsolutePath()));
                            imageMessage.setDate(date);
                            messages.add(imageMessage);
                        }
                    }
                }
                MensagemAdapter adapter = new MensagemAdapter(MensagemActivity.this, messages);
                mensagemAdapter = adapter;
                listaMensagens.setAdapter(adapter);
                listaMensagens.setSelection(messages.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void definirBarraFerramentas() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LinearLayout layout = findViewById(R.id.tool_bar_child);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MensagemActivity.this, DetalhesDeUsuarioActivity.class);
                intent.putExtra("phone", telefone);
                intent.putExtra("name", nome);
                startActivity(intent);
            }
        });

        TextView textView = findViewById(R.id.nome);
        textView.setText(nome);

        ImageButton button = findViewById(R.id.botao_voltar);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MensagemActivity.this.onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.barra_buscar).getVisibility() != View.GONE)
            limparPesquisa();
        else
            super.onBackPressed();
    }

    //onclick listeners

    public void botaoEnviar(View view) {
        attachmentGrid.setVisibility(View.GONE);
        String message = mensagemTextView.getText().toString();
        if(!message.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.put("type", "text");
            map.put("message", message);

            String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            dbRef1.child(n).setValue(map);
            dbRef2.child(n).setValue(map);

            mensagemTextView.setText("");
        }
    }

    public void botaoAnexar(View view) {
        if(attachmentGrid.getVisibility() == View.VISIBLE) {
            attachmentGrid.setVisibility(View.GONE);
        }
        else {
            attachmentGrid.setVisibility(View.VISIBLE);
        }
    }

    public void botaoCamera(View view) {
        attachmentGrid.setVisibility(View.GONE);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA);
    }

    public void botaoGaleria(View view) {
        attachmentGrid.setVisibility(View.GONE);
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, GALLERY);
    }

    public void botaoLocalizacao(View view) {
        attachmentGrid.setVisibility(View.GONE);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        else compartilharLocalizacao();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) compartilharLocalizacao();
                else Toast.makeText(this, "Voce precisa permitir o compartilhamento da localizacao", Toast.LENGTH_SHORT).show();
        }
    }

    private void compartilharLocalizacao() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            try {
                                String lat = Double.toString(location.getLatitude());
                                String lng = Double.toString(location.getLongitude());
                                String url = "http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=15&size=200x200&sensor=false";
                                Map<String, String> map = new HashMap<>();
                                map.put("type", "text");
                                map.put("message", url);

                                String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                                dbRef1.child(n).setValue(map);
                                dbRef2.child(n).setValue(map);
                            }
                            catch (Exception ex) {
                                Toast.makeText(MensagemActivity.this, "Compartilhar localizacao falhou. Asserte que o google play services pode acessar sua localizacao", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        catch (SecurityException ex) {}
    }
}