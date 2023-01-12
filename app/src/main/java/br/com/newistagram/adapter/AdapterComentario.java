package br.com.newistagram.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.newistagram.R;
import br.com.newistagram.model.Comentario;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.MyVieHolder> {

    private List<Comentario> listaComentario;
    private Context context;

    public AdapterComentario(List<Comentario> listaComentario, Context context) {
        this.listaComentario = listaComentario;
        this.context = context;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentario, parent, false);
        return new MyVieHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, int position) {

        Comentario comentario = listaComentario.get(position);

        Uri urlFotoPerfil = Uri.parse(comentario.getCaminhoFoto());
        Glide.with(context).load(urlFotoPerfil).into(holder.fotoPerfil);

        //Em vez de escrever a linha 43 e 44 podia escrever só essa
//        Glide.with(context).load(comentario.getCaminhoFoto()).into(holder.fotoPerfil);

        holder.nome.setText(comentario.getNomeUsuario());
        holder.cometario.setText(comentario.getComentario());
    }

    @Override
    public int getItemCount() {
        return listaComentario.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder{

        CircleImageView fotoPerfil;
        TextView nome, cometario;

        public MyVieHolder(@NonNull View itemView) {
            super(itemView);

            fotoPerfil =  itemView.findViewById(R.id.image_perfil_comentario);
            nome = itemView.findViewById(R.id.text_nome_perfil_comentario);
            cometario = itemView.findViewById(R.id.text_comentario);

        }
    }
}
