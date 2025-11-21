package xsolution.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DB {

    private static Connection conn = null;

    private static Properties loadProperties() {
        try (InputStream input = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            if (input == null) {
                System.err.println("ERRO CRÍTICO: Arquivo db.properties não encontrado em src/main/resources");
                return null;
            }
            props.load(input);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar configurações do banco: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        Properties props = loadProperties();

        if (props == null)
            return null;

        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPass = props.getProperty("db.password");

        if (dbUrl == null || dbUser == null || dbPass == null) {
            System.err.println("ERRO: As chaves db.url, db.user ou db.password não foram encontradas no db.properties");
            return null;
        }

        String url = dbUrl.replace("\"", "").trim();
        String user = dbUser.replace("\"", "").trim();
        String pass = dbPass.replace("\"", "").trim();

        try {
            Class.forName("org.postgresql.Driver");

            return DriverManager.getConnection(url, user, pass);

        } catch (ClassNotFoundException e) {
            System.err.println("Driver do PostgreSQL não encontrado! Verifique o pom.xml.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Erro SQL ao conectar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        closeConnection(conn);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResults(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}