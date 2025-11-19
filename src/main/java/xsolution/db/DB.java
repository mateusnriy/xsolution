package xsolution.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.github.cdimascio.dotenv.Dotenv;
import xsolution.exception.DbException;

public class DB {
  private static Connection conn = null;

  public static Connection getConnection() {
    if (conn == null) {
      try {
        Dotenv dotenv = Dotenv.load();
        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");

        if (dbUrl == null || dbUser == null || dbPassword == null) {
          throw new DbException("Variáveis de ambiente (DB_URL, DB_USER, DB_PASSWORD) não encontradas no .env");
        }

        conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
      } catch (SQLException e) {
        throw new DbException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
      }
    }

    return conn;
  }

  public static void closeConnection() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new DbException("Erro ao fechar conexão" + e.getMessage(), e);
      }
    }
  }

  public static void closeStatement(Statement st) {
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        throw new DbException("Erro ao fechar o Statement: " + e.getMessage(), e);
      }
    }
  }

  public static void closeResults(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        throw new DbException("Erro ao fechar o ResultSet: " + e.getMessage(), e);
      }
    }
  }
}
