package it.corsojava.cashreg.core.implementation.engine;

import it.corsojava.cashreg.core.Scontrino;
import it.corsojava.cashreg.core.implementation.ScontrinoImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcService {

    private static String url;
    private static String userName;
    private static String userPass;

    public static void setUrl(String url) {
        JdbcService.url = url;
    }

    public static void setUserName(String userName) {
        JdbcService.userName = userName;
    }

    public static void setUserPass(String userPass) {
        JdbcService.userPass = userPass;
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,userName,userPass);
    }


}
