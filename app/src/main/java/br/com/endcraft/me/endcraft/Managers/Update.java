package br.com.endcraft.me.endcraft.Managers;

/**
 * Created by JonasXPX on 20.jul.2017.
 */

public class Update {

    private final String version;
    private boolean newVersion;

    public Update(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public boolean hasNewVersion(){
        return newVersion;
    }

    public void setNewVersion(boolean b){
        this.newVersion = b;
    }

}