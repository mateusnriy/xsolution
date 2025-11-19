package xsolution.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DB {
    private static final String URL = "jdbc:postgresql://localhost:5432/xsolution_db";
    private static final String USER = "xsolution_admin";
    private static final String PASSWORD = "M1nhaS3nhaF0rte!#";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Compatibilidade: método sem argumentos chamado por Main.stop().
     * Não temos uma conexão global gerenciada aqui, portanto apenas ignora.
     */
    public static void closeConnection() {
        // Nenhuma ação necessária — conexões devem ser fechadas pelos proprietários.
    }

    public static void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public static void closeResults(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
}
