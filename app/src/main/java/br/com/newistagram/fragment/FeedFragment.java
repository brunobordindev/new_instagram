package br.com.newistagram.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.adapter.AdapterFeed;
import br.com.newistagram.databinding.FragmentFeedBinding;
import br.com.newistagram.databinding.FragmentPostagemBinding;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Feed;


public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;
    private AdapterFeed adapterFeed;
    private List<Feed> listaFeed = new ArrayList<>();
    private ValueEventListener valueEventListenerFeed;
    private DatabaseReference feedRef;
    private String idUsuarioLogado;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater,  container, false);

        //configuracoes iniciais
        idUsuarioLogado = UsuarioFirebase.identificadorUsuario();
        feedRef = ConfiguracaoFirebase.getFirebase()
                .child("feed")
                .child(idUsuarioLogado);

        //configuracao adapter
        adapterFeed = new AdapterFeed(listaFeed, getActivity());

        //configuracao recycler
        binding.recyclerFeed.setHasFixedSize(true);
        binding.recyclerFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerFeed.setAdapter(adapterFeed);


        return binding.getRoot();
    }

    private void listarFeed(){

        valueEventListenerFeed = feedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaFeed.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    listaFeed.add(ds.getValue(Feed.class));
                }

                //esse codigo modifica a lista do recycler 1 2 3 4 ... para ... 4 3 2 1
                Collections.reverse(listaFeed);

                adapterFeed.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        listarFeed();
    }

    @Override
    public void onStop() {
        super.onStop();
        feedRef.removeEventListener(valueEventListenerFeed);
    }
}