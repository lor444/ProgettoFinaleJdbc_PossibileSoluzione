package it.corsojava.cashreg.core.implementation;

import it.corsojava.cashreg.core.Riga;
import it.corsojava.cashreg.core.Scontrino;

public class ModelFactory {

    public static Scontrino createNewScontrino(String id){
        ScontrinoImpl impl=new ScontrinoImpl();
        impl.setId(id);
        return impl;
    }

    public static Scontrino assignId(Scontrino s, String id){
        if(s!=null && s instanceof ScontrinoImpl){
            ((ScontrinoImpl)s).setId(id);
        }
        return s;
    }

    public static Riga createNewRiga(){
        return new RigaImpl();
    }
}
