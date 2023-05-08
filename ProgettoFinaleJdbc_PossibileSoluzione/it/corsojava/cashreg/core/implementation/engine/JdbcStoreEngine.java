package it.corsojava.cashreg.core.implementation.engine;

import com.mysql.cj.protocol.Resultset;
import it.corsojava.cashreg.core.Riga;
import it.corsojava.cashreg.core.Scontrino;
import it.corsojava.cashreg.core.StatoScontrino;
import it.corsojava.cashreg.core.TipiScontrino;
import it.corsojava.cashreg.core.datatypes.base.PercOutOfRangeException;
import it.corsojava.cashreg.core.datatypes.specifici.Iva;
import it.corsojava.cashreg.core.datatypes.specifici.Sconto;
import it.corsojava.cashreg.core.datatypes.specifici.ScontoOutOfRangeException;
import it.corsojava.cashreg.core.implementation.ModelFactory;
import it.corsojava.cashreg.core.implementation.StoreEngine;
import it.corsojava.cashreg.core.implementation.exceptions.StoreEngineLoadException;
import it.corsojava.cashreg.core.implementation.exceptions.StoreEngineSaveScontrinoException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcStoreEngine implements StoreEngine {

    @Override
    public Scontrino saveScontrino(Scontrino s) throws StoreEngineSaveScontrinoException {
        try (Connection cn = JdbcService.getConnection()){
            cn.setAutoCommit(false);
            int id=getNextId(cn,"scontrini","id");
            ModelFactory.assignId(s,""+id); // set ID

            StringBuilder sql=new StringBuilder();
            sql.append("INSERT INTO scontrini ");
            sql.append("(id,tipo,stato,intestazione,piedipagina,dataemissione,oraemissione) ");
            sql.append(" VALUES (?,?,?,?,?,?,?)");
            PreparedStatement ps = cn.prepareStatement(sql.toString());
            ps.setInt(1,id);
            ps.setString(2,s.getTipo().toString());
            ps.setString(3,StatoScontrino.REGISTRATO.toString());
            ps.setString(4,s.getIntestazione());
            ps.setString(5,s.getPieDiPagina());
            ps.setDate(6,java.sql.Date.valueOf(s.getData()));
            ps.setTime(7,java.sql.Time.valueOf(s.getOra()));
            ps.executeUpdate();
            s.setStato(StatoScontrino.REGISTRATO);

            saveRows(s,cn);  // salva le righe nella medesima transazione
            cn.commit();
            return s;
        }catch(SQLException sqle){
            throw new StoreEngineSaveScontrinoException("Impossibile registrare lo scontrino",sqle);
        }
    }

    private void saveRows(Scontrino s, Connection cn) throws SQLException{
        StringBuilder sql=new StringBuilder();
        sql.append("INSERT INTO righe ");
        sql.append("(idRiga,idScontrino,prezzoUnitario,sconto,iva,quantita,descrizione) ");
        sql.append("VALUES (?,?,?,?,?,?,?)");
        PreparedStatement pr=cn.prepareStatement(sql.toString());
        int rid=getNextId(cn,"righe","idRiga");
        for(Riga r : s.getRighe()){
            pr.setInt(1,rid);
            pr.setInt(2,Integer.parseInt(s.getId()));
            pr.setBigDecimal(3,BigDecimal.valueOf(r.getPrezzoUnitario()));
            pr.setBigDecimal(4, BigDecimal.valueOf(r.getSconto()!=null ? r.getSconto().getValue() : 0.0));
            pr.setBigDecimal(5,ivaToNumber(r.getIva()));
            pr.setBigDecimal(6, BigDecimal.valueOf(r.getQuantita()));
            pr.setString(7,r.getDescrizione());
            pr.executeUpdate();
            rid++;
        }
    }

    @Override
    public List<Scontrino> loadAll() throws StoreEngineLoadException {
        List<Scontrino> elenco=new ArrayList<Scontrino>();
        try (Connection cn = JdbcService.getConnection()){
            StringBuilder sql=new StringBuilder();
            sql.append("SELECT * ");
            sql.append("FROM scontrini S ");
            sql.append("LEFT JOIN righe R on S.id=R.idScontrino ");
            sql.append("ORDER BY S.id,R.idRiga ASC");
            Statement st=cn.createStatement();
            ResultSet rs=st.executeQuery(sql.toString());
            Scontrino currentScontrino=null;

            while(rs.next()){
                String id=rs.getString("id");
                if(currentScontrino==null || currentScontrino.getId().compareTo(id)!=0){
                    currentScontrino=ModelFactory.createNewScontrino(id);
                    currentScontrino.setIntestazione(rs.getString("intestazione"));
                    currentScontrino.setPieDiPagina(rs.getString("piedipagina"));
                    currentScontrino.setTipo(TipiScontrino.valueOf(rs.getString("tipo")));
                    currentScontrino.setStato(StatoScontrino.valueOf(rs.getString("stato")));
                    currentScontrino.setData(rs.getDate("dataEmissione").toLocalDate());
                    currentScontrino.setOra(rs.getTime("oraEmissione").toLocalTime());
                    elenco.add(currentScontrino);
                }
                try {
                    currentScontrino.getRighe().add(caricaRiga(rs));
                }catch (PercOutOfRangeException sore){
                    throw  new StoreEngineLoadException("Nello scontrino con id "+id+" una riga presenta uno sconto non corretto",sore);
                }
            }
            rs.close();
        }catch(SQLException sqle){
            throw new StoreEngineLoadException("Impossibile caricare l'elenco scontrini.",sqle);
        }
        return elenco;
    }

    private Riga caricaRiga(ResultSet rs) throws SQLException,PercOutOfRangeException{
        Riga r = ModelFactory.createNewRiga();
        r.setPrezzoUnitario(rs.getBigDecimal("prezzoUnitario").doubleValue());
        Sconto sconto = new Sconto(rs.getBigDecimal("sconto").doubleValue());
        r.setSconto(sconto);
        r.setIva(ivaFromNumber(rs.getBigDecimal("iva")));
        r.setQuantita(rs.getBigDecimal("quantita").doubleValue());
        r.setDescrizione(rs.getString("descrizione"));
        return r;
    }


    private static BigDecimal ivaToNumber(Iva iva){
        return switch (iva){
            case IVA_0 -> BigDecimal.ZERO;
            case IVA_4 -> BigDecimal.valueOf(4.0);
            case IVA_10 -> BigDecimal.TEN;
            case IVA_22 -> BigDecimal.valueOf(22.0);
            default -> BigDecimal.ZERO;
        } ;
    }

    private static Iva ivaFromNumber(BigDecimal number){
        double d=number.doubleValue();
        Map<Double,Iva> ivaMap=new HashMap<Double,Iva>();
        ivaMap.put(22.0,Iva.IVA_22);
        ivaMap.put(10.0,Iva.IVA_10);
        ivaMap.put(4.0,Iva.IVA_4);
        ivaMap.put(0.0,Iva.IVA_0);
        return ivaMap.get(d);
    }

    private int getNextId(Connection cn,String tabName,String colName) throws SQLException{
        ResultSet rs= cn.createStatement().executeQuery("SELECT max("+colName.trim()+") as M FROM "+tabName.trim());
        rs.next();
        return rs.getInt(1)+1;
    }

}
