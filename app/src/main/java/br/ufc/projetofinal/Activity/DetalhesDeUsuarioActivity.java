package br.ufc.projetofinal.Activity;

import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import br.ufc.projetofinal.R;

public class DetalhesDeUsuarioActivity extends AppCompatActivity {

    private String telefone;
    private String nome;
    private File foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_de_usuario);

        Intent intent = getIntent();
        nome = (String) intent.getSerializableExtra("name");
        telefone = (String)intent.getSerializableExtra("phone");

        TextView textView = findViewById(R.id.nome);
        textView.setText(nome);
        textView = findViewById(R.id.telefone);
        textView.setText(telefone);

        final TextView status = findViewById(R.id.status);
        status.setText("");

        FirebaseDatabase.getInstance().getReference("users").child(telefone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    status.setText(dataSnapshot.child("status").getValue(String.class));
                }
                catch (Exception ex) {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.foto_contato);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, telefone + ".jpg");
        foto = file;
        if(file.exists()) {
            imageView.setImageURI(Uri.parse(file.getAbsolutePath()));
        }

        setMedia();
    }

    private void setMedia() {
        FirebaseDatabase.getInstance().getReference("messages").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + telefone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                LinearLayout linearLayout = findViewById(R.id.media);
                ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                File file = wrapper.getDir(telefone,MODE_PRIVATE);
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.child("type").getValue(String.class).equals("image")) {
                        String[] key = ds.getKey().split(" ");
                        File file1 = new File(file, key[2] + " " + key[0] + " " + key[1] + ".jpg");
                        if(file1.exists()) {
                            i++;
                            ImageView fotoView = new ImageView(DetalhesDeUsuarioActivity.this);
                            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(200, 200);
                            fotoView.setLayoutParams(layoutParams);
                            fotoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            fotoView.setTag(file1.getAbsolutePath());
                            fotoView.setImageURI(Uri.parse(file1.getAbsolutePath()));
                            fotoView.setPadding(5, 5, 5, 5);
                            fotoView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String s = (String) v.getTag();
                                    if(s != null && new File(s).exists()) {
                                        Intent intent = new Intent(DetalhesDeUsuarioActivity.this, MostrarImagemActivity.class);
                                        intent.putExtra("image", s);
                                        startActivity(intent);
                                    }
                                }
                            });
                            linearLayout.addView(fotoView);
                        }
                    }
                }
                if(i == 0) {
                    ((LinearLayout) findViewById(R.id.media_parent)).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void clicouFoto(View view) {
        if(foto.exists()) {
            Intent intent = new Intent(this, MostrarImagemActivity.class);
            intent.putExtra("image", foto.getAbsolutePath());
            startActivity(intent);
        }
    }
}
