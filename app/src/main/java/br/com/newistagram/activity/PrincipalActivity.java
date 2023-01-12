package br.com.newistagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import br.com.newistagram.R;
import br.com.newistagram.databinding.ActivityPrincipalBinding;
import br.com.newistagram.fragment.FeedFragment;
import br.com.newistagram.fragment.PerfilFragment;
import br.com.newistagram.fragment.PesquisaFragment;
import br.com.newistagram.fragment.PostagemFragment;
import br.com.newistagram.helper.ConfiguracaoFirebase;

public class PrincipalActivity extends AppCompatActivity {

    private ActivityPrincipalBinding binding;
    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_principal);

        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Instagram");
        setSupportActionBar(toolbar);
        configuracaoBottonNavigation();

        //comeca com p fragment home (feed)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.view_pager, new FeedFragment()).commit();
    }

    private void configuracaoBottonNavigation(){
        BottomNavigationView navView = findViewById(R.id.nav_view);

        habilitarNavegacao(navView);

        //conf item selecionado inicialmente 0 - home / 1 -pesquisa / 2 - postagem e 3 - perfil
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
    }

    private void habilitarNavegacao(BottomNavigationView view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                switch (item.getItemId()){
                    case R.id.ic_home:
                        fragmentTransaction.replace(R.id.view_pager, new FeedFragment()).commit();
                        return true;
                    case R.id.ic_pesquisa:
                        fragmentTransaction.replace(R.id.view_pager, new PesquisaFragment()).commit();
                        return true;
                    case R.id.ic_postagem:
                        fragmentTransaction.replace(R.id.view_pager, new PostagemFragment()).commit();
                        return true;
                    case R.id.ic_perfil:
                        fragmentTransaction.replace(R.id.view_pager, new PerfilFragment()).commit();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_sair:
                auth.signOut();
                voltarInicio();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void voltarInicio() {
        startActivity(new Intent( getApplicationContext(), MainActivity.class));
    }
}