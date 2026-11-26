package com.aydsii.demo.config;

import lombok.Getter;
//singleton, unica intancia con las reglas respecto a descuento mayorista que usa el carrito
@Getter
public class ConfiguracionNegocio {

    // la unica instancia, creada al cargar la clase y al tener el contructor privado es imposible crear un nuevo objeto
    private static final ConfiguracionNegocio UNICA = new ConfiguracionNegocio();
    
    private final int cantidadMayorista = 30;
    private final float descuentoMayorista = 0.15f;

    private ConfiguracionNegocio() {
    }

    public static ConfiguracionNegocio getUnica() {
        return UNICA;
    }
}
