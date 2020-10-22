package br.ufc.projetofinal.Activity;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import br.ufc.projetofinal.Service.DBHelper;
import br.ufc.projetofinal.R;
import br.ufc.projetofinal.Service.SincronizarMensagensService;
import de.hdodenhof.circleimageview.CircleImageView;

import static br.ufc.projetofinal.Service.DBHelper.NOME_BD;

public class ConfiguracoesActivity extends AppCompatActivity {
    private File imagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracoes);

        TextView editar =  findViewById(R.id.editarTextView);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfiguracoesActivity.this, EditarContaActivity.class);
                startActivity(intent);
            }

        });

        TextView apagar = findViewById(R.id.apagarTextView);
        apagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ConfiguracoesActivity.this)
                        .setTitle("Apagar conta?")
                        .setMessage("Quer mesmo apagar a conta?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteAccount();
                            }})
                        .setNegativeButton("Nao", null).show();
            }
        });

        TextView convidar = findViewById(R.id.convidarTextView);
        convidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("inviteLink").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String link = "";
                        try {
                            link = dataSnapshot.getValue(String.class);
                        } catch (Exception ex) {}

                        Intent intent = new AppInviteInvitation.IntentBuilder("Olha esse app legal")
                                .setMessage("Um novo app de mensagens. " + link)
                                .setCallToActionText("Compartilhar")
                                .build();
                        startActivityForResult(intent, 2);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final TextView nome = findViewById(R.id.nomeTextView);
        final TextView status = findViewById(R.id.status);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nome.setText(dataSnapshot.child("name").getValue(String.class));
                status.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        CircleImageView fotoPerfil = findViewById(R.id.fotoPerfil);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");
        imagem = file;
        if(file.exists()) {
            fotoPerfil.setImageURI(Uri.parse(file.getAbsolutePath()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void clicouFoto(View view) {
        if(imagem.exists()) {
            Intent intent = new Intent(this, MostrarImagemActivity.class);
            intent.putExtra("image", imagem.getAbsolutePath());
            startActivity(intent);
        }
    }

    public void deleteAccount() {
        String usuario = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(usuario);
        databaseReference.child("name").setValue(null);
        databaseReference.child("status").setValue(null);

        databaseReference = FirebaseDatabase.getInstance().getReference("messages");
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT Friend FROM Message", null);
        while (cursor.moveToNext()) {
            String friend = cursor.getString(cursor.getColumnIndex("Friend"));
            databaseReference.child(usuario + " " + friend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.child("type").getRef().setValue(null);
                        ds.child("message").getRef().setValue(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        cursor.close();
        FirebaseAuth.getInstance().signOut();
        try {
            deleteDatabase(NOME_BD);
        }
        catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        if (file.isDirectory())
        {
            String[] children = file.list();
            for (String child : children)
            {
                new File(file, child).delete();
            }
        }
        stopService(new Intent(this, SincronizarMensagensService.class));
        finishAffinity();
    }
}
