package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.adapter.AdapterMiniaturasFiltros;
import br.com.newistagram.databinding.ActivityFiltroBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.RecyclerItemClickListener;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Postagem;
import br.com.newistagram.model.Usuario;



public class FiltroActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private ActivityFiltroBinding binding;
    private Bitmap imagem;
    private Bitmap imagemFiltro;

    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference firebaseRef;
    private Usuario usuarioLogado;
    private AlertDialog dialog;
    private DataSnapshot seguidoresSnapshot;

    private List<ThumbnailItem> listaFiltros;

    private AdapterMiniaturasFiltros adapterMiniaturasFiltros;

    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filtro );

        //configuracoes iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        listaFiltros = new ArrayList<>();
        idUsuarioLogado = UsuarioFirebase.identificadorUsuario();
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Filtros");
        setSupportActionBar(toolbar);

        //recuperar dados do usuario logado
//        recuperarDadosUsuarioLogado(); tinha aqui só que mudou de nome   recuperarDadosPostagem();
        //recuoerar dados para uma nova postagem
        recuperarDadosPostagem();

        //mexer no manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //recupera a imagem escolhida pelo usuario no fragment PostagemFragment
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
            imagem = BitmapFactory.decodeByteArray(dadosImagem, 0 , dadosImagem.length );
            binding.imageFotoEscolhida.setImageBitmap(imagem);

            //colocamos aqui tb, pq se ele nao escolhe um filtro na foto da erro
            imagemFiltro = imagem.copy(imagem.getConfig(), true);

            //configura recleyView de filtros
            adapterMiniaturasFiltros = new AdapterMiniaturasFiltros(listaFiltros, getApplicationContext());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            binding.recyclerFiltro.setLayoutManager(layoutManager);
            binding.recyclerFiltro.setAdapter(adapterMiniaturasFiltros);

            //clicar nos filtros (recyclerView) e modificar a foto padrao com o filtro escolhido
            binding.recyclerFiltro.addOnItemTouchListener(new RecyclerItemClickListener(
                    getApplicationContext(),
                    binding.recyclerFiltro,
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            ThumbnailItem item = listaFiltros.get(position);

                            imagemFiltro = imagem.copy(imagem.getConfig(), true);
                            Filter filtro = item.filter;
                            binding.imageFotoEscolhida.setImageBitmap(filtro.processFilter(imagemFiltro));
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }

                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        }
                    }

            ));

            //recupera os filtros
            recuperarFiltros();

        }
    }

    private void abrirDialogCarregamento(String titulo){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);
        alert.setCancelable(false);
        alert.setView(R.layout.carregamento);

        dialog = alert.create();
        dialog.show();
    }

    //recuperarDadosUsuarioLogado()
    private void recuperarDadosPostagem(){

        abrirDialogCarregamento("Carregando dados, aguarde!");
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //recupera dados do usuario logado
                usuarioLogado = snapshot.getValue(Usuario.class);

                //recuperar os seguidores
                DatabaseReference seguidoresRef = firebaseRef
                        .child("seguidores")
                        .child(idUsuarioLogado);
                seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        seguidoresSnapshot = snapshot;
                        dialog.cancel();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void recuperarFiltros() {

        //limpar itens
        ThumbnailsManager.clearThumbs();
        listaFiltros.clear();

        //configurando filtro - Normal
        ThumbnailItem item = new ThumbnailItem();
        item.image = imagem;
        item.filterName = "Normal";
        ThumbnailsManager.addThumb(item);

        //Lista de todos os filtros
        //Deois do Filter.Pack. (vem o get do filtro ), mas usamos o getFilterPack pq ele pega a lista e nao um unico filtro.
        List<Filter> filtros = FilterPack.getFilterPack(getApplicationContext());
        for (Filter filtro : filtros){
            ThumbnailItem itemFiltro = new ThumbnailItem();
            itemFiltro.image = imagem;
            itemFiltro.filter = filtro;
            itemFiltro.filterName = filtro.getName();
            ThumbnailsManager.addThumb(itemFiltro);


        }

        listaFiltros.addAll(ThumbnailsManager.processThumbs(getApplicationContext()));
        adapterMiniaturasFiltros.notifyDataSetChanged();
    }

    private void publicarPostagem() {

        abrirDialogCarregamento("Salvando postagem!");
        Postagem postagem = new Postagem();
        postagem.setIdUsuario(idUsuarioLogado);
        postagem.setDescricao(binding.textDescricaoFiltro.getText().toString());

        //Recuperar dados da imagem para  salvar no firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemFiltro.compress(Bitmap.CompressFormat.JPEG, 72, baos);
        byte[] dadosImagem = baos.toByteArray();

        //salvar imagem no firebaseStorage
        StorageReference storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        final StorageReference imageRef = storageRef
                .child("imagens")
                .child("postagens")
                .child(postagem.getId() + ".jpeg");

        UploadTask uploadTask = imageRef.putBytes(dadosImagem);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Erro ao salvar imagem, tente novamente!", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        //recupera local da foto
                        Uri url = task.getResult();
                        postagem.setCaminhoFoto(url.toString());

                        //atualizar qts de postagens
                        int qtdPostagens = usuarioLogado.getPostagens() + 1 ;
                        usuarioLogado.setPostagens(qtdPostagens);
                        usuarioLogado.atualizarQtdPostagem();

                        //salvar postagem
                        if (postagem.salvar(seguidoresSnapshot)){

                            Toast.makeText(getApplicationContext(), "Sucesso ao salvar postagens!", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_salvar_postagem:
                publicarPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}