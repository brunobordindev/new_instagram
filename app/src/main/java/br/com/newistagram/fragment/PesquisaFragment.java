package br.com.newistagram.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.newistagram.R;
import br.com.newistagram.activity.PerfilAmigoActivity;
import br.com.newistagram.adapter.AdapterPesquisa;
import br.com.newistagram.databinding.FragmentPesquisaBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.RecyclerItemClickListener;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Usuario;

public class PesquisaFragment extends Fragment {

    private FragmentPesquisaBinding binding;
    private List<Usuario> listaUsuarios;
    private DatabaseReference usuarioRef;
    private AdapterPesquisa adapterPesquisa;

    private String idUsuarioLogado;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPesquisaBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        listaUsuarios = new ArrayList<>();
        usuarioRef = ConfiguracaoFirebase.getFirebase().child("usuarios");
        idUsuarioLogado = UsuarioFirebase.identificadorUsuario();

        //configuracao recycler
        binding.recyclerPesquisa.setHasFixedSize(true);
        binding.recyclerPesquisa.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapterPesquisa = new AdapterPesquisa(listaUsuarios, getActivity());
        binding.recyclerPesquisa.setAdapter(adapterPesquisa);

        //clicle recyclerView usuarios
        binding.recyclerPesquisa.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                binding.recyclerPesquisa,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        Intent intent = new Intent(getActivity(), PerfilAmigoActivity.class);
                        intent.putExtra("usuarioSelecionado", usuarioSelecionado);
                        startActivity(intent);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }));



        //Configuracao searchView
        binding.searchViewPesquisa.setQueryHint(getString(R.string.buscar_usuario));
        binding.searchViewPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Log.d("onQueryTextSubmit", "text digitado: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Log.d("onQueryTextChange", "text digitado: " +newText);

                String textoDigitado = newText.toUpperCase();
                pesquisarUsuario(textoDigitado);
                return true;
            }
        });



        return binding.getRoot();
    }

    private void pesquisarUsuario(String textoDigitado) {

        //limpar lista
        listaUsuarios.clear();

        //Pesquisa usuários caso tenha texto na pesquisa
        if (textoDigitado.length() > 0 ){

            Query query = usuarioRef.orderByChild("nomePesquisa")
                    .startAt(textoDigitado)
                    .endAt(textoDigitado + "\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //limpar lista
                    listaUsuarios.clear();

                    for (DataSnapshot ds : snapshot.getChildren()){

                        //verifica se é o usuario logado e tira da lista
                        Usuario usuario = ds.getValue(Usuario.class);
                        if (idUsuarioLogado.equals(usuario.getId()))
                            continue;

                        listaUsuarios.add(usuario);
                    }


                    adapterPesquisa.notifyDataSetChanged();
//                    int total = listaUsuarios.size();
//                    Log.i("totalUsuarios", "total: " + total);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            listaUsuarios.clear();
            adapterPesquisa.notifyDataSetChanged();
        }
    }
}