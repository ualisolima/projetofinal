package br.ufc.projetofinal.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import br.ufc.projetofinal.R;

public class VerificarNumeroDeTelefoneActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private static String code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificar_numero_telefone);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        final String string = (String) intent.getSerializableExtra("phone");
        TextView verificar = findViewById(R.id.verificar);
        verificar.setText("Verificar " + string);
        TextView telefone = findViewById(R.id.telefone);
        telefone.setText(string);

        EditText codigo = findViewById(R.id.code);
        codigo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 6) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(code, s.toString());
                    mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Intent intent = new Intent(VerificarNumeroDeTelefoneActivity.this, SalvarDadosDoPerfilActivity.class);
                            intent.putExtra("phone", string);
                            startActivity(intent);
                            System.out.println("aqui 1");
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VerificarNumeroDeTelefoneActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        PhoneAuthProvider.getInstance().verifyPhoneNumber(string, 60, TimeUnit.SECONDS, this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        mAuth.signInWithCredential(credential).addOnCompleteListener(VerificarNumeroDeTelefoneActivity.this,
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Intent intent = new Intent(VerificarNumeroDeTelefoneActivity.this, SalvarDadosDoPerfilActivity.class);
                                        intent.putExtra("phone", string);
                                        startActivity(intent);
                                        System.out.println("aqui 2");
                                        finish();
                                    }
                                });
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(VerificarNumeroDeTelefoneActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        code = s;
                    }
                });
    }

    public void numeroErrado(View view) {
        onBackPressed();
    }
}
