package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import br.com.newistagram.R;
import br.com.newistagram.databinding.ActivityEditarPerfilBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.Permissao;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Usuario;

public class EditarPerfilActivity extends AppCompatActivity {

    private static final int SELECAO_GALERIA = 200;
    private ActivityEditarPerfilBinding binding;
    private Usuario usuarioLogado;
    private StorageReference storageRef;
    private String identificadorUsuario;
    private DatabaseReference usuarioRef;
    private String bio;

    private String[] permissoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editar_perfil);

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Editar Perfil");
        setSupportActionBar(toolbar);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.identificadorUsuario();

        usuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("usuarios")
                .child(usuarioLogado.getId());
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Usuario usuario = snapshot.getValue(Usuario.class);
                bio = String.valueOf(usuario.getMensagemBio());

                if (usuario.getMensagemBio() == null){
                    binding.editBio.getText();
                }else{
                    binding.editBio.setText(bio);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //mexer no manifests
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        //Recuperar os dados do usuario
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        binding.editNomeAlteracao.setText(user.getDisplayName());

        //Recuperar os dados do usuario da foto
        Uri url  =  user.getPhotoUrl();
        if (url != null){
            Glide.with(EditarPerfilActivity.this)
                    .load(url)
                    .into(binding.imgEditarFoto);
        }else{

            binding.imgEditarFoto.setImageResource(R.drawable.avatar);
        }

        binding.btnSalvarAlteracoes.setOnClickListener(view -> {

            String nomeAlterado = binding.editNomeAlteracao.getText().toString();
            String bioAlterado = binding.editBio.getText().toString();

            //Atualizar nome do perfil
            UsuarioFirebase.atualizarNomeUsuario(nomeAlterado);
            usuarioLogado.setNome(nomeAlterado);
            usuarioLogado.setNomePesquisa(nomeAlterado.toUpperCase());
            usuarioLogado.setMensagemBio(bioAlterado);
            usuarioLogado.atualizar();
            finish();
        });

        binding.btnAlterarFoto.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, SELECAO_GALERIA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                //Seleciona apenas da galeria
                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //caso tenha sido escolhido uma imagem
                if (imagem != null){

                    //configura imagem para aparecer na tela
                    binding.imgEditarFoto.setImageBitmap(imagem);

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte[] dadosImagem = baos.toByteArray();


                    //salvar imagem no firebase
                    final StorageReference imageRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario + ".jpeg");
                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getApplicationContext(), "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizarFotoUsuario(url);
                                }

                            });

                            Toast.makeText(getApplicationContext(), "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void atualizarFotoUsuario(Uri url) {

        //atualizar foto no perfil
        UsuarioFirebase.atualizarFotoUsuario(url);

        //Atualizar foto no Firebase
        usuarioLogado.setCaminhoFoto(url.toString());
        usuarioLogado.atualizar();

        Toast.makeText(getApplicationContext(), "Sua foto foi atualizada!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}