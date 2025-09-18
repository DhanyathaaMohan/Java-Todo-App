package com.todo.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection{
    public static final String URL="jdbc:mysql://localhost:3306/todoapp";
    public static final String USERNAME= "root";
    public static final String PASSWORD="Kunjumani@16";

    static 
    {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("JDBC Driver is missing");
        }
    }
    public static Connection getDBConnection() throws SQLException
    {
        return DriverManager.getConnection(URL,USERNAME,PASSWORD);
    }
}