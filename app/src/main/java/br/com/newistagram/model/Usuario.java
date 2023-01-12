package br.com.newistagram.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import br.com.newistagram.helper.ConfiguracaoFirebase;
import br.com.newistagram.helper.UsuarioFirebase;

public class Usuario  implements Serializable {

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String mensagemBio;
    private String caminhoFoto;
    private String nomePesquisa;
    private int postagens = 0;
    private int seguidores = 0;
    private int seguindo  = 0;

    public Usuario() {
    }

    public void salvar(){

        DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
        reference.child("usuarios")
                .child(this.id)
                .setValue(this);
    }

    public void atualizar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        Map objeto = new HashMap();

        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        objeto.put("/usuarios/" + getId() + "/nome", getNome());
        objeto.put("/usuarios/" + getId() + "/caminhoFoto", getCaminhoFoto());
        objeto.put("/usuarios/" + getId() + "/mensagemBio", getMensagemBio());

        firebaseRef.updateChildren(objeto);

    }

    public void atualizarQtdPostagem(){

        DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
        DatabaseReference usuarioRef =  reference
                .child("usuarios")
                .child(getId());

        HashMap<String, Object> dados = new HashMap<>();
        dados.put("postagens", getPostagens());

        usuarioRef.updateChildren(dados);
    }

    public Map<String, Object> converterParaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("caminhoFoto", getCaminhoFoto());
        usuarioMap.put("id", getId());
        usuarioMap.put("mensagemBio", getMensagemBio());
        usuarioMap.put("nomePesquisa", getNomePesquisa());
        usuarioMap.put("postagens", getPostagens());
        usuarioMap.put("seguidores", getSeguidores());
        usuarioMap.put("seguindo", getSeguindo());
        return usuarioMap;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getMensagemBio() {
        return mensagemBio;
    }

    public void setMensagemBio(String mensagemBio) {
        this.mensagemBio = mensagemBio;
    }

    public String getNomePesquisa() {
        return nomePesquisa;
    }

    public void setNomePesquisa(String nomePesquisa) {
        this.nomePesquisa = nomePesquisa.toUpperCase();
    }

    public int getPostagens() {
        return postagens;
    }

    public void setPostagens(int postagens) {
        this.postagens = postagens;
    }

    public int getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(int seguidores) {
        this.seguidores = seguidores;
    }

    public int getSeguindo() {
        return seguindo;
    }

    public void setSeguindo(int seguindo) {
        this.seguindo = seguindo;
    }
}
