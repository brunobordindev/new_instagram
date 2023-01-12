package br.com.newistagram.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;

import br.com.newistagram.R;
import br.com.newistagram.activity.FiltroActivity;
import br.com.newistagram.databinding.FragmentPostagemBinding;
import br.com.newistagram.helper.Permissao;


public class PostagemFragment extends Fragment {

    private FragmentPostagemBinding binding;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private String[] permissoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostagemBinding.inflate(inflater,  container, false);
        binding.setLifecycleOwner(this);

        //validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, getActivity(), 1);

        //manifest e classe permissao
        binding.tnAbrirCamera.setOnClickListener(view ->{
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getActivity().getPackageManager()) != null){

                startActivityForResult(i, SELECAO_CAMERA);
            }
        });

        //manifest e classe permissao
        binding.btnAbrirGaleria.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (i.resolveActivity(getActivity().getPackageManager()) != null){

                startActivityForResult(i, SELECAO_GALERIA);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK){

            Bitmap imagem = null;

            try {

                //valida tipo seleção da imagem
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagemSelecionada);
                        break;
                }

                //valida imagem selecionada
                if (imagem != null){

                    //converte imagem em byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //envia a imagem escolhida para a activity de filtro
                    Intent i = new Intent(getActivity(), FiltroActivity.class);
                    i.putExtra("fotoEscolhida", dadosImagem);
                    startActivity(i);

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}