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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import br.com.newistagram.R;
import br.com.newistagram.databinding.ActivityLoginBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.model.Usuario;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login );

        binding.textCadastrar.setOnClickListener( view -> {
            startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
        });

        binding.editEmail.requestFocus();
        binding.progressBarLogin.setVisibility(View.GONE);
        binding.btnLogin.setOnClickListener(view -> {

            String email = binding.editEmail.getText().toString();
            String senha = binding.editPassword.getText().toString();

            if (!email.isEmpty()){
                if (!senha.isEmpty()){

                    usuario = new Usuario();
                    usuario.setEmail(email);
                    usuario.setSenha(senha);
                    validarLogin();

                }else{
                    Toast.makeText(getApplicationContext(), "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
            }
        });

        int fechado = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;

        binding.btnVisibilitySenha.setOnClickListener(view -> {
            if (binding.editPassword.getInputType() == fechado ){
                binding.editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnVisibilitySenha.setImageResource(R.drawable.ic_baseline_visibility_24);
            }else{
                binding.editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                binding.btnVisibilitySenha.setImageResource(R.drawable.ic_baseline_visibility_off_24);
            }
        });
    }

    private void validarLogin() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            binding.progressBarLogin.setVisibility(View.VISIBLE);
                            verificarUsuarioLogado();
                            Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();

                        }else{
                            String excecao = "";
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthInvalidUserException e){
                                excecao = "Usuário não está cadastrado";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "E-mail e senha não corresponde a um usuário cadastrado";
                            }catch (Exception e){
                                excecao = "Erro ao logar usuário" + e.getMessage();
                            }

                            Toast.makeText(getApplicationContext(), excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    private void verificarUsuarioLogado() {
        FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
//        auth.signOut();
        if (auth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
            finish();
        }
    }
}