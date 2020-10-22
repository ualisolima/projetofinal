package br.ufc.projetofinal.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import br.ufc.projetofinal.R;

import static br.ufc.projetofinal.Activity.MensagemActivity.GALLERY;

public class EditarContaActivity extends AppCompatActivity {
    DatabaseReference dbRef;
    private static ImageView fotoView;

    private String nome = "";
    private String status = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editarconta);
        fotoView = new ImageView(this);
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        TextView atualizarNome = findViewById(R.id.atualizarNome);
        atualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarCaixaDialogo("Nome");
            }
        });

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nome = dataSnapshot.child("name").getValue(String.class);
                status = dataSnapshot.child("status").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TextView atualizarFoto = findViewById(R.id.atualizarFoto);
        atualizarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm.getActiveNetworkInfo() != null) {
                    mostrarDialogoMudarFoto();
                }
                else {
                    Toast.makeText(EditarContaActivity.this, "No connection available", Toast.LENGTH_LONG).show();
                }
            }
        });

        TextView atualizarStatus = findViewById(R.id.updateStatus);
        atualizarStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarCaixaDialogo("Status");
            }
        });
    }

    public void mostrarCaixaDialogo(final String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        final EditText input = new EditText(this);
        input.setHint("Novo " + title);
        if(title.equals("Nome")) input.setText(nome);
        else input.setText(status);
        builder.setView(input);
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(title.equals("Nome")) {
                    mudarNome(input.getText().toString());
                }
                else {
                    mudarStatus(input.getText().toString());
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void mudarNome(String title) {
        dbRef.child("name").setValue(null);
        dbRef.child("name").setValue(title);
        Toast.makeText(this, "Nome salvo", Toast.LENGTH_SHORT).show();
    }

    public void mudarStatus(String status) {
        dbRef.child("status").setValue(null);
        dbRef.child("status").setValue(status);
        Toast.makeText(this, "Status salvo", Toast.LENGTH_SHORT).show();
    }

    public void mostrarDialogoMudarFoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto de perfil");

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");

        if(file.exists()) fotoView.setImageURI(Uri.parse(file.getAbsolutePath()));
        else fotoView.setImageResource(R.drawable.conta);
        fotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY);
            }
        });

        builder.setView(fotoView);
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mudarFotoPerfil();
            }
        });
        builder.setNegativeButton("Canclear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void mudarFotoPerfil() {
        final Bitmap foto = ((BitmapDrawable) fotoView.getDrawable()).getBitmap();
        StorageReference stgRef = FirebaseStorage.getInstance().getReference("profilePictures").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            foto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final ProgressDialog dialog = ProgressDialog.show(this, "Por favor espere", "Carregando Imagem...", true);
            UploadTask uploadTask = stgRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(EditarContaActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                    File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
                    file = new File(file, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");
                    OutputStream stream = null;
                    try {
                        stream = new FileOutputStream(file);
                        foto.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    }
                    catch (Exception ex) {
                        Toast.makeText(EditarContaActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        try {
                            dialog.dismiss();
                            Toast.makeText(EditarContaActivity.this, "Foto salva", Toast.LENGTH_SHORT).show();
                            stream.flush();
                            stream.close();
                        }catch (Exception ex) {}
                    }
                }
            });
        }
        catch (Exception ex) {
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY) {
                Uri imageUri = data.getData();
                fotoView.setImageURI(imageUri);
            }
        }
    }
}


