package br.ufc.projetofinal.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

import br.ufc.projetofinal.R;

public class MostrarImagemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_foto);

        Intent intent = getIntent();
        String path = (String) intent.getSerializableExtra("image");
        PhotoView view = (PhotoView) findViewById(R.id.foto);
        view.setImageURI(Uri.parse(path));
    }
}
