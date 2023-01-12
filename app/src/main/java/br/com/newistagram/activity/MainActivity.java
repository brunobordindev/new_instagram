package br.com.newistagram.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import br.com.newistagram.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                abrirAuntenticacao();
            }
        }, 2000);
    }

    private void abrirAuntenticacao(){
        Intent abrir = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(abrir);
        finish();
    }
}