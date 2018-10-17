package com.vlim.controldetiempos;

public class Config {
    //DB Version
    static int VERSION_DB = 1;
    static String TAG = "SIGA";
    //private static final String HOST = "http://www.sigaapp.com/";
    private static final String HOST = "http://54.183.159.116:8182/SIGAv2/";

    // Servicios
    public static final String LOGIN_URL   = HOST + "loginApp";  //54.165.114.152
    public static final String CAT_PROYECTO_URL  = HOST + "actividad/catProyectos";
    public static final String CAT_CLIENTE_URL   = HOST + "actividad/catClientes";
    public static final String GUARDA_REG_URL       = HOST + "actividad/inReg";
}
