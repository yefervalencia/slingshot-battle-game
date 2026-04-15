package com.slingshot;

public class Launcher {
    public static void main(String[] args) {
        // Este intermediario engaña a la Máquina Virtual de Java
        // para que cargue correctamente las librerías de JavaFX
        // antes de iniciar la aplicación real.
        AppFX.main(args);
    }
}