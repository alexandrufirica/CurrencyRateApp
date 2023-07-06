package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CurrencyService {

    public Connection con;

    public void Connect(){
        try{
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/CurrencyRates", "postgres", "admin");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
