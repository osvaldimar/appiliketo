package com.mobile.iliketo.appiliketo.model;

/**
 * Created by OSVALDIMAR on 8/28/2015.
 */
public class User {

    private String username;
    private String password;
    private int ativado;

    public User(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public int getAtivado() {
        return ativado;
    }

    public void setAtivado(int ativado) {
        this.ativado = ativado;
    }
}
