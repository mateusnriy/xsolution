package xsolution.utils;

import java.io.IOException;
import java.net.URL;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ViewUtils {

    public static void carregarCena(Event event, String fxmlPath, String titulo) {
        try {
            URL resource = ViewUtils.class.getResource(fxmlPath);
            if (resource == null) {
                AlertUtils.showError("Erro Crítico", "Arquivo FXML não encontrado: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            if (titulo != null) {
                stage.setTitle(titulo);
            }
            
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erro de Navegação", "Não foi possível carregar a tela: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    public static Parent carregarView(String fxmlPath) {
        try {
            URL resource = ViewUtils.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("Arquivo FXML não encontrado: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erro Interno", "Falha ao carregar componente visual: " + e.getMessage());
            return null;
        }
    }

    public static boolean abrirModal(String fxmlPath, String titulo, Event eventOwner) {
        try {
            URL resource = ViewUtils.class.getResource(fxmlPath);
            
            // DIAGNÓSTICO DE ERRO: Verifica se o arquivo existe antes de carregar
            if (resource == null) {
                System.err.println("ERRO: Arquivo FXML não encontrado: " + fxmlPath);
                AlertUtils.showError("Erro de Arquivo", "Não foi possível encontrar a tela:\n" + fxmlPath);
                return false;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            
            // Configura comportamento de Modal (bloqueia a janela de trás)
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Tenta definir o Owner (pai) para centralizar corretamente
            if (eventOwner != null && eventOwner.getSource() instanceof Node) {
                Stage ownerStage = (Stage) ((Node) eventOwner.getSource()).getScene().getWindow();
                stage.initOwner(ownerStage);
            }
            
            stage.setResizable(false);
            stage.showAndWait(); // Espera fechar
            
            return true; // Sucesso

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erro ao abrir tela", "Falha técnica: " + e.getMessage());
            return false;
        }
    }
}