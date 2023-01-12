package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.adapter.AdapterGrid;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.SquareImageView;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Postagem;
import br.com.newistagram.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private TextView btnSeguir, nome, bio, postagens, seguidores, seguindo;
    private GridView gridViewPerfil;
    private CircleImageView foto;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private DatabaseReference seguidoresRef;
    private DatabaseReference firebaseRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference postagensUsuarioref;
    private AdapterGrid adapterGrid;


    private String idUsuarioLogado;

    private List<Postagem> listaPostagens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //config nome do usuario na toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Editar Perfil");
        setSupportActionBar(toolbar);

        //Conifguracoes inicias para recuperar dados do perfil amigo
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.identificadorUsuario();

        //instanciando
        instanciando();

        //mexer no manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //Recuperar usuario selecionado
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");

            //congigurar a referencia postagens usuario
            postagensUsuarioref = ConfiguracaoFirebase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());

            getSupportActionBar().setTitle(usuarioSelecionado.getNome());
            nome.setText(usuarioSelecionado.getNome());

            if (usuarioSelecionado.getCaminhoFoto() != null){

                Uri uri = Uri.parse(usuarioSelecionado.getCaminhoFoto());
                Glide.with(getApplicationContext()).load(uri).into(foto);

            }else{
                foto.setImageResource(R.drawable.avatar);
            }

        }

        //Inicializar image loader
        inicializacaoImageLoader();

        //carregar as fotos das postagens de um usuario
        carregarFotosPostagens();

        //Abre foto clicada
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // i é position
                Postagem postagem = listaPostagens.get(i);
                Intent intent = new Intent(getApplicationContext(), VisualizarPostagemActivity.class );
                intent.putExtra("postagem", postagem);
                intent.putExtra("usuario", usuarioSelecionado);
                startActivity(intent);

            }
        });
    }

    //instancia a UniversalImageLoader - GridView - precisa permissao no manifest
    public  void inicializacaoImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public void carregarFotosPostagens(){

        listaPostagens = new ArrayList<>();

        //recupera as fotos postadas pelo usuario
        postagensUsuarioref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<String> urlsFotos = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    listaPostagens.add(postagem);
                    urlsFotos.add(postagem.getCaminhoFoto());
//                    Log.i("postagem", "url:" + postagem.getCaminhoFoto());
                }

                Collections.reverse(urlsFotos);

                //configurar adapter
                adapterGrid = new AdapterGrid(getApplicationContext(), R.layout.adapter_grid, urlsFotos);
                gridViewPerfil.setAdapter(adapterGrid);
                Collections.reverse(listaPostagens);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //recupera dados do usuario logado
                usuarioLogado = snapshot.getValue(Usuario.class);

                //verifica se usuario ja está seguindo o amigo selecionado.
                verificaSegueUsuarioAmigo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //se o usuario segue esse amigo
    private void verificaSegueUsuarioAmigo(){

        //quero acessar um seguidor especifico amigo
        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);

        //consulta apenas uma unica vez o dados
        seguidorRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){

                            //Usuario amigo já está sendo seguindo
                            habiltarBotaoUsuario(true);
                        }else{

                            //Usuario amigo não está sendo seguindo
                           habiltarBotaoUsuario(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void habiltarBotaoUsuario(boolean segueUsuario){
        if (segueUsuario){
            btnSeguir.setText(R.string.seguindo);
        }else{
            btnSeguir.setText(R.string.seguir);
            btnSeguir.setBackgroundColor(getResources().getColor(R.color.blue_btn));

            //Adiciona evento para seguir usuário
            btnSeguir.setOnClickListener(view -> {

                //Salvar seguidor
                salvarSeguidor(usuarioLogado, usuarioSelecionado);

            });
        }
    }

    private void salvarSeguidor(Usuario userLogado, Usuario userAmigo){

        /*
            seguidores
                id_usuario_amigo_a_seguir
                    id_usuario_logado
                        dados do amigo a seguir
         */
        HashMap<String, Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome", userLogado.getNome());
        dadosUsuarioLogado.put("caminhoFoto", userLogado.getCaminhoFoto());
        dadosUsuarioLogado.put("mensagemBio", userLogado.getMensagemBio());

        DatabaseReference seguidorRef = seguidoresRef
                .child(userAmigo.getId())
                .child(userLogado.getId());
        seguidorRef.setValue(dadosUsuarioLogado);

        //alterar botao acao para seguindo
        btnSeguir.setText(R.string.seguindo);
        btnSeguir.setBackgroundResource(R.drawable.box_button);
        //remove a acao do botao, nao vai conseguir sair
        btnSeguir.setOnClickListener(null);

        //Incrementar seguindo do usuario  logado
        int seguindo = userLogado.getSeguindo() + 1;
        HashMap<String, Object> dadosSeguindoUserLogado = new HashMap<>();
        dadosSeguindoUserLogado.put("seguindo", seguindo);
        DatabaseReference userLogadoSeguindo = usuariosRef.child(userLogado.getId());
        userLogadoSeguindo.updateChildren(dadosSeguindoUserLogado);

        //Incrementar seguidor ao usuario  amigo
        int seguidor = userAmigo.getSeguidores() + 1;
        HashMap<String, Object> dadosSeguidorUserAmigo = new HashMap<>();
        dadosSeguidorUserAmigo.put("seguidores", seguidor);
        DatabaseReference userAmigoGanhaSeguidor = usuariosRef.child(userAmigo.getId());
        userAmigoGanhaSeguidor.updateChildren(dadosSeguidorUserAmigo);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //recuoperar dados do amigo selecionado
        recuperaDadosPerfilAmigo();

        //recuperar dados usuario logado
        recuperarDadosUsuarioLogado();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    //recuperar dados do perfil amigo
    private void recuperaDadosPerfilAmigo(){

        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Usuario usuario = snapshot.getValue(Usuario.class);

                        String recuperarPostagem = String.valueOf(usuario.getPostagens());
                        String recuperarSeguidores = String.valueOf(usuario.getSeguidores());
                        String recuperarSeguindo= String.valueOf(usuario.getSeguindo());
                        String bioMesg = String.valueOf(usuario.getMensagemBio());

                        postagens.setText(recuperarPostagem);
                        seguidores.setText(recuperarSeguidores);
                        seguindo.setText(recuperarSeguindo);
                        bio.setText(bioMesg);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

    }

    private void instanciando() {
        btnSeguir = findViewById(R.id.btn_acao_perfil);
        nome = findViewById(R.id.edit_nome_perfil);
        bio = findViewById(R.id.text_bio_perfil);
        foto = findViewById(R.id.img_perfil);
        postagens = findViewById(R.id.edit_number_postagens);
        seguidores = findViewById(R.id.edit_number_seguidores);
        seguindo = findViewById(R.id.edit_number_seguindo);
        gridViewPerfil = findViewById(R.id.grid_view_perfil);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}