package br.com.endcraft.me.endcraft;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JonasXPX on 18.jul.2017.
 */

public class Movie implements Serializable{

    private String link = "";
    private String nome = "";
    private String idioma = "";
    private String imgLink = "";
    private String bannerLink = "";
    private String desc = "";
    private List<Categoria> categorias = null;

    public Movie(){  }

    public Movie(String nome, String imgLink){
        this.nome = nome;
        this.imgLink = imgLink;
    }

    public String getBannerLink() {
        return bannerLink;
    }

    public void setBannerLink(String bannerLink) {
        this.bannerLink = bannerLink;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    @Override
    public String toString() {
        return nome;
    }
}
