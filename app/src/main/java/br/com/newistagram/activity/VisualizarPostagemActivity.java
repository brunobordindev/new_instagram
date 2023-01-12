package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.databinding.ActivityVisualizarPostagemBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Feed;
import br.com.newistagram.model.Postagem;
import br.com.newistagram.model.PostagemCurtida;
import br.com.newistagram.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class VisualizarPostagemActivity extends AppCompatActivity {

    private ActivityVisualizarPostagemBinding binding;
    private TextView textPerfilPostagem, textNomePerfilDescricao, textDescricaoPostagem, textQtdCurtidas;
    private CircleImageView imagePerfilPostagem;
    private ImageView imagePostagemSelecionada;
    private LikeButton likeButton;
    private List<Feed> listaFeed = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_visualizar_postagem);

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Visualizar Postagem");
        setSupportActionBar(toolbar);

        textPerfilPostagem = findViewById(R.id.text_perfil_postagem);
        textNomePerfilDescricao = findViewById(R.id.text_nome_perfil_descricao);
        textDescricaoPostagem = findViewById(R.id.text_descricao_postagem);
        imagePerfilPostagem = findViewById(R.id.image_perfil_postagem);
        imagePostagemSelecionada = findViewById(R.id.image_postagem_selecionada);
        likeButton = findViewById(R.id.like_button_feed);
        textQtdCurtidas = findViewById(R.id.text_qts_curtidas);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            Postagem postagem = (Postagem) bundle.getSerializable("postagem");
            Usuario usuario = (Usuario) bundle.getSerializable("usuario");

            //exibe dados de usuário
            Uri uri = Uri.parse(usuario.getCaminhoFoto());
            Glide.with(getApplicationContext())
                    .load(uri)
                    .into(imagePerfilPostagem);
            textPerfilPostagem.setText(usuario.getNome());
            textNomePerfilDescricao.setText(usuario.getNome() + " -");

            //exibe dados da postagem
            Uri uriPostagem = Uri.parse(postagem.getCaminhoFoto());
            Glide.with(getApplicationContext())
                    .load(uriPostagem)
                    .into(imagePostagemSelecionada);
            textDescricaoPostagem.setText(postagem.getDescricao());

             // Referência no firebase para curtidas
             final Usuario usarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

            //recupera dados da postagem curtida
            DatabaseReference curtidasRef = ConfiguracaoFirebase.getFirebase()
                    .child("postagens-curtidas")
                    .child(postagem.getId());
            curtidasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                     int qtdCurtidas = 0;
                     if(snapshot.hasChild("qtdCurtidas")){
                         PostagemCurtida postagemCurtida = snapshot.getValue(PostagemCurtida.class);
                         qtdCurtidas = postagemCurtida.getQtdCurtidas();
                     }


                    //verifica se ja foi clicado
                    if (snapshot.hasChild(usarioLogado.getId())){
                        likeButton.setLiked(true);
                    }else{
                        likeButton.setLiked(false);
                    }

                    //funciona para marcar e desmarcar
                    Feed feed = new Feed();
                    feed.setId(postagem.getId());

                    PostagemCurtida curtida = new PostagemCurtida();
                    curtida.setFeed(feed);
                    curtida.setUsuario(usarioLogado);
                    curtida.setQtdCurtidas(qtdCurtidas);

                    likeButton.setOnLikeListener(new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {
                            curtida.salvar();
                            textQtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                        }

                        @Override
                        public void unLiked(LikeButton likeButton) {
                            curtida.remover();
                            textQtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                        }
                    });


                    textQtdCurtidas.setText(qtdCurtidas + " curtidas");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}