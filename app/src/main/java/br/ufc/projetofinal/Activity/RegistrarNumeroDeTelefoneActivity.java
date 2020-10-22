package br.ufc.projetofinal.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import br.ufc.projetofinal.R;

public class RegistrarNumeroDeTelefoneActivity extends AppCompatActivity {

    private String[] locales;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_numero_telefone);

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().getReference("users").keepSynced(true);
            FirebaseDatabase.getInstance().getReference("inviteLink").keepSynced(true);
        }
        catch (Exception ex) {}

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, ConversaActivity.class));
            finish();
        }

        criarListaDePaises();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){}
                else finish();
        }
    }

    private void criarListaDePaises() {
        ArrayList<String> paises = carregarNomesPaises();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, paises);

        Spinner paisSpinner = findViewById(R.id.pais);
        paisSpinner.setAdapter(adapter);

        paisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String paisID = locales[position];
                String[] rl = getResources().getStringArray(R.array.CodigoPaises);
                for(String x : rl){
                    String[] g = x.split(",");
                    if(g[1].trim().equals(paisID.trim())){
                        TextView textView = findViewById(R.id.codigoPais);
                        textView.setText("+" + g[0]);
                        return;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void nextButton(View view) {
        TextView codigoPais = findViewById(R.id.codigoPais);
        EditText telefone =  findViewById(R.id.telefone);

        Intent intent = new Intent(this, VerificarNumeroDeTelefoneActivity.class);
        intent.putExtra("phone", codigoPais.getText().toString() + telefone.getText().toString());
        startActivity(intent);
    }

    public ArrayList<String> carregarNomesPaises() {

        locales = Locale.getISOCountries();
        ArrayList<String> countries = new ArrayList<>();

        for (String countryCode : locales) {

            Locale obj = new Locale("", countryCode);
            countries.add(obj.getDisplayCountry());
        }
        return countries;
    }

    public static class HelperClass {
        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        public static Bitmap decodeBitmapFromFile(File file, int reqWidth, int reqHeight) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            }
            catch (Exception ex) {
                return null;
            }
        }
        }
}
