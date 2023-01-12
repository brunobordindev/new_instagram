package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.adapter.AdapterComentario;
import br.com.newistagram.databinding.ActivityComentariosBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Comentario;
import br.com.newistagram.model.Usuario;

public class ComentariosActivity extends AppCompatActivity {

    private ActivityComentariosBinding binding;
    private String idPostagem;
    private Usuario usuario;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentario = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private ValueEventListener valueEventListenerComentario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comentarios);

        firebaseRef = ConfiguracaoFirebase.getFirebase();

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Comentários");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //config adapter
        adapterComentario = new AdapterComentario(listaComentario, getApplicationContext());

        //config recycler
        binding.recyclerComentario.setHasFixedSize(true);
        binding.recyclerComentario.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComentario.setAdapter(adapterComentario);

        //configuracoes iniciais
        usuario = UsuarioFirebase.getDadosUsuarioLogado();

        //recupera id da postagem
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            idPostagem = bundle.getString("idPostagem");
        }



        binding.btnEnviarComentario.setOnClickListener(view -> {
            String textoComentario = binding.editComentario.getText().toString();
            if (!textoComentario.isEmpty()){

                Comentario comentario = new Comentario();
                comentario.setIdPostagem(idPostagem);
                comentario.setIdUsuario(usuario.getId());
                comentario.setNomeUsuario(usuario.getNome());
                comentario.setCaminhoFoto(usuario.getCaminhoFoto());
                comentario.setComentario(textoComentario);

                if (comentario.salvar()){

                    Toast.makeText(getApplicationContext(), "Comentário salvo com  sucesso!", Toast.LENGTH_SHORT).show();

                }else{

                    Toast.makeText(getApplicationContext(), "Erro ao salvar comentário!", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(getApplicationContext(), "Escreva um comentário!", Toast.LENGTH_SHORT).show();
            }

            //Limpa comentário digitado
            binding.editComentario.setText("");
        });
    }

    private void recuperarComentarios(){

        comentariosRef = firebaseRef.child("comentarios")
                .child(idPostagem);
        valueEventListenerComentario =  comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaComentario.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    listaComentario.add(ds.getValue(Comentario.class));
                }
                adapterComentario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComentarios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentario);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}