package br.com.newistagram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

import br.com.newistagram.R;


public class AdapterGrid extends ArrayAdapter<String> {

    private Context context;
    private int layoutResource;
    private List<String> urlFotos;

    public AdapterGrid(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResource = resource;
        this.urlFotos = objects;
    }

    public AdapterGrid(@NonNull FragmentActivity context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResource = resource;
        this.urlFotos = objects;
    }

    public class MyViewHolder{
        ImageView imagem;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //caso a view nao esteja inflata, precisamos inflar
        MyViewHolder myViewHolder;
        if (convertView == null){
            myViewHolder = new MyViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            myViewHolder.progressBar = convertView.findViewById(R.id.progress_grid_perfil);
            myViewHolder.imagem = convertView.findViewById(R.id.image_grid_perfil);

            convertView.setTag(myViewHolder);

        }else{

            myViewHolder = (MyViewHolder) convertView.getTag();
        }

        //recuperar dados da imagem
        String urlImagem = getItem(position);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(
                urlImagem,
                myViewHolder.imagem,
                new ImageLoadingListener() {
            @Override
            //começa o processo de carregamento da imagem
            public void onLoadingStarted(String imageUri, View view) {
                myViewHolder.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            //acontece algum erro ou falha na hora de carregar a imagem
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                myViewHolder.progressBar.setVisibility(View.GONE);
                myViewHolder.imagem.setImageResource(R.drawable.foto_falha);
            }

            @Override
            //quando é completado o carregamento da imagem
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                myViewHolder.progressBar.setVisibility(View.GONE);
            }

            @Override
            //quando é cancelado o carregamento da imagem
            public void onLoadingCancelled(String imageUri, View view) {
                myViewHolder.progressBar.setVisibility(View.GONE);
            }
        });

        return convertView;
    }
}
