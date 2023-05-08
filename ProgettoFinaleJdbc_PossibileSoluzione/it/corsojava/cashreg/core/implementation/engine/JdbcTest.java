package it.corsojava.cashreg.core.implementation.engine;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

public class JdbcTest {
    public static void main(String[] args) throws Exception{
        JdbcService.setUrl("jdbc:postgresql://localhost/cashreg");
        JdbcService.setUserName("corsosql");
        JdbcService.setUserPass("corsosql");

        ResultSet rs = JdbcService.getConnection().createStatement().executeQuery("select * from scontrini");

        System.out.println("SCONTRINI----------------------------------");
        ResultSetMetaData md = rs.getMetaData();
        for(int i=1;i<=md.getColumnCount();i++){
            System.out.print(md.getColumnName(i)+" | ");
            System.out.print(md.getColumnType(i) + " | ");
            System.out.print(md.getColumnName(i) + " | ");
            System.out.print(md.getColumnClassName(i) );
            System.out.println();
        }
        rs.close();
        System.out.println("RIGHE----------------------------------");
        rs = JdbcService.getConnection().createStatement().executeQuery("select * from righe");

        md = rs.getMetaData();
        for(int i=1;i<=md.getColumnCount();i++){
            System.out.print(md.getColumnName(i)+" | ");
            System.out.print(md.getColumnType(i) + " | ");
            System.out.print(md.getColumnName(i) + " | ");
            System.out.print(md.getColumnClassName(i) );
            System.out.println();
        }

    }
}
