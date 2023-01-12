package br.com.newistagram.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.com.newistagram.R;
import br.com.newistagram.activity.EditarPerfilActivity;
import br.com.newistagram.activity.VisualizarPostagemActivity;
import br.com.newistagram.adapter.AdapterGrid;
import br.com.newistagram.databinding.FragmentPerfilBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Postagem;
import br.com.newistagram.model.Usuario;


public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private Usuario usuarioLogado;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListenerPerfil;
    private DatabaseReference postagensUsuarioref;
    private AdapterGrid adapterGrid;

    private List<Postagem> listaPostagem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        //Configuracoes iniciais para recuperar os dados do usuarioLogado
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");

        //congigurar a referencia postagens usuario
        postagensUsuarioref = ConfiguracaoFirebase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());

        binding.btnAcaoPerfil.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), EditarPerfilActivity.class));
        });

        //Inicializar image loader
        inicializacaoImageLoader();

        //carregar as fotos das postagens de um usuario
        carregarFotosPostagens();

        binding.gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Postagem postagem = listaPostagem.get(i);
                Intent intent = new Intent(getActivity(), VisualizarPostagemActivity.class);
                intent.putExtra("postagem", postagem);
                intent.putExtra("usuario", usuarioLogado);
                startActivity(intent);
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarDadosUsuarioPerfilLogado();

        //recuprerar foto do usuario
        recuperarFoto();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }


    private void recuperarFoto(){

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //recuperar foto do usuario
        String foto = usuarioLogado.getCaminhoFoto();
        if (foto != null && foto != ""){
            Uri url = Uri.parse(foto);
            Glide.with(getActivity())
                    .load(url)
                    .into(binding.imgPerfil);

        }else{
            binding.imgPerfil.setImageResource(R.drawable.avatar);
        }
    }

    private void recuperarDadosUsuarioPerfilLogado(){

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Usuario usuario = snapshot.getValue(Usuario.class);
                String nome = String.valueOf(usuario.getNome());
                String bioMesg = String.valueOf(usuario.getMensagemBio());
                String recuperarSeguidores = String.valueOf(usuario.getSeguidores());
                String recuperarSeguindo = String.valueOf(usuario.getSeguindo());

                binding.editNomePerfil.setText(nome);
                binding.textBioPerfil.setText(bioMesg);
                binding.editNumberSeguidores.setText(recuperarSeguidores);
                binding.editNumberSeguindo.setText(recuperarSeguindo);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //instancia a UniversalImageLoader - GridView - precisa permissao no manifest
    public  void inicializacaoImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getActivity())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public void carregarFotosPostagens(){

        listaPostagem = new ArrayList<>();

        //recupera as fotos postadas pelo usuario
        postagensUsuarioref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<String> urlsFotos = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    listaPostagem.add(postagem);
                    urlsFotos.add(postagem.getCaminhoFoto());
//                    Log.i("postagem", "url:" + postagem.getCaminhoFoto());
                }

                Collections.reverse(urlsFotos);

                int qtdPostagens =  urlsFotos.size();
                binding.editNumberPostagens.setText(String.valueOf(qtdPostagens));

                //configurar adapter
                adapterGrid = new AdapterGrid(getActivity(), R.layout.adapter_grid, urlsFotos);
                binding.gridViewPerfil.setAdapter(adapterGrid);
                Collections.reverse(listaPostagem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}