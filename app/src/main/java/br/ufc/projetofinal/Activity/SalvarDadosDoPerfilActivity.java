package br.ufc.projetofinal.Activity;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import br.ufc.projetofinal.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class SalvarDadosDoPerfilActivity extends AppCompatActivity {
    private EditText nome;
    private CircleImageView fotoPerfil;
    private DatabaseReference dbRef;
    String telefone;
    private StorageReference stgRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salvar_dados_perfil);
        dbRef = FirebaseDatabase.getInstance().getReference();
        stgRef = FirebaseStorage.getInstance().getReference();
        Intent intent = getIntent();
        telefone = (String) intent.getSerializableExtra("phone");
        nome = findViewById(R.id.nome);
        fotoPerfil = findViewById(R.id.foto);
        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1);
            }
        });
    }

    public void nextButton(View view) {
        BitmapDrawable drawable = (BitmapDrawable) fotoPerfil.getDrawable();
        if(drawable == null) {
            Toast.makeText(this, "Deve selecionar uma foto", Toast.LENGTH_SHORT).show();
            return;
        }
        final Bitmap photo = drawable.getBitmap();
        final String nam = nome.getText().toString();
        if(nam.length() == 0) {
            Toast.makeText(this, "Deve colocar nome", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference reference = stgRef.child("profilePictures/" + telefone + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final ProgressDialog dialog = ProgressDialog.show(this, "Por favor aguarde", "Carregando Imagem...", true);
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SalvarDadosDoPerfilActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
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
                        photo.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    }
                    catch (Exception ex) {
                        Toast.makeText(SalvarDadosDoPerfilActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        try {
                            dialog.dismiss();
                            stream.flush();
                            stream.close();
                        }catch (Exception ex) {}
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("name", nam);
                    map.put("status", "Disponivel");
                    DatabaseReference reference1 = dbRef.child("users").child(telefone);

                    reference1.child("name").setValue(null);
                    reference1.child("status").setValue(null);
                    dbRef.child("users").child(telefone).setValue(map);
                    startActivity(new Intent(SalvarDadosDoPerfilActivity.this, ConversaActivity.class));
                    finish();
                }
            });
        }
        catch (Exception ex) {
            Toast.makeText(SalvarDadosDoPerfilActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                Uri imageUri = data.getData();
                fotoPerfil.setImageURI(imageUri);
            }
        }
    }
}
