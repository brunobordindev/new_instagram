package br.com.newistagram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

import br.com.newistagram.R;

public class AdapterMiniaturasFiltros extends RecyclerView.Adapter<AdapterMiniaturasFiltros.MyViewHolder> {

    private List<ThumbnailItem> listaFiltros;
    private Context context;

    public AdapterMiniaturasFiltros(List<ThumbnailItem> listaFiltros, Context context) {
        this.listaFiltros = listaFiltros;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_miniaturas_filtros, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ThumbnailItem item = listaFiltros.get(position);
        holder.nomeFiltro.setText(item.filterName);
        holder.foto.setImageBitmap(item.image);
    }

    @Override
    public int getItemCount() {
        return listaFiltros.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView foto;
        TextView nomeFiltro, nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeFiltro = itemView.findViewById(R.id.text_nome_filtro);
            foto = itemView.findViewById(R.id.image_foto_filtro);

        }
    }
}
