package br.com.newistagram.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.activity.ComentariosActivity;
import br.com.newistagram.activity.VisualizarPostagemActivity;
import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;
import br.com.newistagram.model.Feed;
import br.com.newistagram.model.PostagemCurtida;
import br.com.newistagram.model.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {

    private List<Feed> listaFeed;
    private Context context;

    public AdapterFeed(List<Feed> listaFeed, Context context) {
        this.listaFeed = listaFeed;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_feed, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final Feed feed = listaFeed.get(position);
        final Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //carrega dados do feed
        Uri uriFotoUsuario = Uri.parse(feed.getFotoUsuario());
        Glide.with(context).load(uriFotoUsuario).into(holder.fotoPerfil);

        Uri uriFotoPostagem = Uri.parse(feed.getFotoPostagem());
        Glide.with(context).load(uriFotoPostagem).into(holder.fotoPostagem);

        holder.nomePerfil.setText(feed.getNomeUsuario());
        holder.nomeDescricao.setText(feed.getNomeUsuario());
        holder.descricao.setText(feed.getDescricao());

        //Adiciona evento de clique nos comentários
        holder.visualizarComentarios.setOnClickListener(view -> {
            Intent i = new Intent(context, ComentariosActivity.class);
            i.putExtra("idPostagem", feed.getId());
            context.startActivity(i);
        });

        /*
        postagens-curtidas
            id_postagem
                + qtd_curtidas
                + id_usuario
                    nome_usuario
                    caminho_foto
         */
        //Recuperar dados da postagem curtida
        DatabaseReference curtidasRef = ConfiguracaoFirebase.getFirebase()
                .child("postagens-curtidas")
                .child(feed.getId());
        curtidasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int qtdCurtidas = 0;
                if (snapshot.hasChild("qtdCurtidas")){
                    PostagemCurtida postagemCurtida = snapshot.getValue(PostagemCurtida.class);
                    qtdCurtidas = postagemCurtida.getQtdCurtidas();
                }

                //verifica se ja foi clicado
                if (snapshot.hasChild(usuarioLogado.getId())){
                    holder.likeButton.setLiked(true);
                }else{
                    holder.likeButton.setLiked(false);
                }

                //Monta objeto postagem curtida
                final PostagemCurtida curtida = new PostagemCurtida();
                curtida.setFeed(feed);
                curtida.setUsuario(usuarioLogado);
                curtida.setQtdCurtidas(qtdCurtidas);


                //adiciona eventos para curtir uma foto
                holder.likeButton.setOnLikeListener(new OnLikeListener() {
                    //curtir
                    @Override
                    public void liked(LikeButton likeButton) {
                        curtida.salvar();
                        holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                        Log.i("likeButton", "liked");
                    }
                    //descurtir
                    @Override
                    public void unLiked(LikeButton likeButton) {
                        curtida.remover();
                        holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
                        Log.i("likeButton", "unLiked");
                    }
                });

                holder.qtdCurtidas.setText(curtida.getQtdCurtidas() + " curtidas");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return listaFeed.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView fotoPerfil;
        TextView nomePerfil, nomeDescricao , descricao, qtdCurtidas;
        ImageView fotoPostagem, visualizarComentarios;
        LikeButton likeButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.image_perfil_postagem);
            nomePerfil = itemView.findViewById(R.id.text_perfil_postagem);
            descricao = itemView.findViewById(R.id.text_descricao_postagem);
            nomeDescricao = itemView.findViewById(R.id.text_nome_perfil_descricao);
            qtdCurtidas = itemView.findViewById(R.id.text_qts_curtidas);
            fotoPostagem = itemView.findViewById(R.id.image_postagem_selecionada);
            visualizarComentarios = itemView.findViewById(R.id.image_comentario_feed);
            likeButton = itemView.findViewById(R.id.like_button_feed);
        }
    }
}
