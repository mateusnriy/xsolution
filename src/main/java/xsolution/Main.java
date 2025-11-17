package xsolution;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");

        if (dbUser == null || dbPassword == null) {
            System.err.println("Erro: As variáveis de ambiente DB_USER e DB_PASSWORD não foram definidas.");
            return;
        }

        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            if (conn != null) {
                System.out.println("Conectado ao banco de dados com sucesso!");
            } else {
                System.out.println("Falha na conexão!");
            }
        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e.getMessage());
        }
    }
}