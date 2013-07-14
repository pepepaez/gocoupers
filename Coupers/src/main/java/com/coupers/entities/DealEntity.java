package com.coupers.entities;

import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by pepe on 7/13/13.
 */
public class DealEntity
{


    public int id_promocion;
    public int id_tipopromocion;
    public String descripcionpromocion;
    public String iniciopromocion;
    public String finpromocion;
    public int cantidad;
    public int valor;
    public int porcentaje;
    public int metapromocion;

    public DealEntity(){}


    public DealEntity(int id_promocion, int id_tipopromocion, String descripcionpromocion, String iniciopromocion,
                      String finpromocion, int cantidad, int valor, int porcentaje, int metapromocion) {

        this.id_promocion = id_promocion;
        this.id_tipopromocion = id_tipopromocion;
        this.descripcionpromocion = descripcionpromocion;
        this.iniciopromocion = iniciopromocion;
        this.finpromocion = finpromocion;
        this.cantidad = cantidad;
        this.valor = valor;
        this.porcentaje = porcentaje;
        this.metapromocion = metapromocion;
    }


    public Object getProperty(int arg0) {

        switch(arg0)
        {
            case 0:
                return id_promocion;
            case 1:
                return id_tipopromocion;
            case 2:
                return descripcionpromocion;
            case 3:
                return iniciopromocion;
            case 4:
                return finpromocion;
            case 5:
                return cantidad;
            case 6:
                return valor;
            case 7:
                return porcentaje;
            case 8:
                return metapromocion;
        }

        return null;
    }

    public int getPropertyCount() {
        return 9;
    }

    public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
        switch(index)
        {
            case 0:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "ID Promocion";
                break;
            case 1:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "ID Tipo Promocion";
                break;
            case 2:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Descripcion Promocion";
                break;
            case 3:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Inicio Promocion";
                break;
            case 4:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Fin Promocion";
                break;
            case 5:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "Cantidad";
                break;
            case 6:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "Valor";
                break;
            case 7:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "Porcentaje";
                break;
            case 8:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "Meta Promocion";
                break;
            default:break;
        }
    }

    public void setProperty(int index, Object value) {
        switch(index)
        {
            case 0:
                id_promocion = Integer.parseInt(value.toString());
                break;
            case 1:
                id_tipopromocion = Integer.parseInt(value.toString());
                break;
            case 2:
                descripcionpromocion = value.toString();
                break;
            case 3:
                iniciopromocion = value.toString();
                break;
            case 4:
                finpromocion = value.toString();
                break;
            case 5:
                cantidad = Integer.parseInt(value.toString());
                break;
            case 6:
                valor = Integer.parseInt(value.toString());
                break;
            case 7:
                porcentaje = Integer.parseInt(value.toString());
                break;
            case 8:
                metapromocion = Integer.parseInt(value.toString());
                break;
            default:
                break;
        }
    }
}