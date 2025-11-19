package xsolution.application;

import java.sql.Connection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import xsolution.db.DB;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // ALTERADO: Inicia pelo MainDashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/MainDashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("X Solution - Sistema de Gestão de TI");
            // Maximizar a tela para melhor visualização do Dashboard
            primaryStage.setMaximized(true); 
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro crítico ao iniciar aplicação: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Testando conexão com banco...");
        Connection conn = DB.getConnection();
        if (conn != null) {
            System.out.println("Conexao OK! Iniciando JavaFX...");
            launch(args);
        } else {
            System.err.println("Falha ao conectar no banco. Verifique o ConnectionFactory.");
        }
    }
}