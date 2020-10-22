package br.ufc.projetofinal.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

public class MeuReceptor extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            context.startService(new Intent(context, SincronizarMensagensService.class));
        }
    }
}
