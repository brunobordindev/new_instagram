package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.newistagram.R;
import br.com.newistagram.databinding.ActivityCadastroBinding;
import br.com.newistagram.helper.Base64Custom;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cadastro);

        int fechado = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;

        binding.editEmailCadastro.requestFocus();
        binding.btnVisibilityPassword.setOnClickListener(view -> {
            if (binding.editPasswordCadastro.getInputType() == fechado ){
                binding.editPasswordCadastro.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnVisibilityPassword.setImageResource(R.drawable.ic_baseline_visibility_24);
            }else{
                binding.editPasswordCadastro.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                binding.btnVisibilityPassword.setImageResource(R.drawable.ic_baseline_visibility_off_24);
            }
        });

        binding.progressBarCadastro.setVisibility(View.GONE);

        binding.btnCadastrar.setOnClickListener(view -> {
            String campoNome = binding.editNomeCadastro.getText().toString();
            String campoEmail = binding.editEmailCadastro.getText().toString();
            String campoSenha = binding.editPasswordCadastro.getText().toString();

            if (!campoNome.isEmpty()){
                if (!campoEmail.isEmpty()){
                    if (!campoSenha.isEmpty()){

                        usuario = new Usuario();
                        usuario.setNome(campoNome);
                        usuario.setEmail(campoEmail);
                        usuario.setSenha(campoSenha);
                        usuario.setNomePesquisa(campoNome.toUpperCase());
                        cadastrar(usuario);

                    }else{
                        Toast.makeText(getApplicationContext(), "Preencha a sua senha!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Preencha o seu e-mail!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Preencha o seu nome!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cadastrar(Usuario usuario) {

        binding.progressBarCadastro.setVisibility(View.VISIBLE);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            try {

//                                String id = Base64Custom.codificadorBase64(usuario.getEmail());
                                String id = task.getResult().getUser().getUid();
                                usuario.setId(id);
                                usuario.salvar();

                                //salvar dados no profile do firebase
                                UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                                Toast.makeText(CadastroActivity.this, "Sucesso ao realizar o cadastro!", Toast.LENGTH_SHORT).show();
                                binding.progressBarCadastro.setVisibility(View.GONE);
                                startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
                                finish();

                            }catch (Exception e ){
                               e.printStackTrace();
                            }


                        }else{

                            String excecao = "";
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                excecao = "Digite uma senha mais forte";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "Digite um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                excecao = "E-mail já cadastrado!";
                            }catch (Exception e){
                                excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                            binding.progressBarCadastro.setVisibility(View.GONE);
                        }
                    }
                }

        );
    }
}