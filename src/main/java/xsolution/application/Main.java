package xsolution.application;

import java.sql.Connection;
import java.io.IOException;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setTitle("X Solution - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML da tela de Login:");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("Fechando a aplicação e a conexão com o banco...");
        DB.closeConnection();
    }
    public static void main(String[] args) {
        System.out.println("Testando conexão com banco...");
        Connection conn = DB.getConnection();
        if (conn != null) {
            System.out.println("Conexao OK! Iniciando JavaFX...");
            launch(args);
        } else {
            System.err.println("Falha ao conectar no banco.");
        }
    }
}

