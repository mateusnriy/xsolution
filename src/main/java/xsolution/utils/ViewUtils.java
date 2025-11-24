package xsolution.utils;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ViewUtils {

    public static void mudarCena(Event event, String fxmlPath, String titulo) {
        try {
            Parent root = loadFxml(fxmlPath);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            if (titulo != null) {
                stage.setTitle(titulo);
            }
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            tratarErro(e, fxmlPath);
        }
    }

    public static Parent carregarView(String fxmlPath) {
        try {
            return loadFxml(fxmlPath);
        } catch (Exception e) {
            tratarErro(e, fxmlPath);
            return null;
        }
    }

    public static <T> T abrirModal(String fxmlPath, String titulo, Consumer<T> initializer, Event eventOwner) {
        try {
            FXMLLoader loader = new FXMLLoader(getResource(fxmlPath));
            Parent root = loader.load();
            
            T controller = loader.getController();
            
            if (initializer != null) {
                initializer.accept(controller);
            }

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            if (eventOwner != null && eventOwner.getSource() instanceof Node) {
                Stage ownerStage = (Stage) ((Node) eventOwner.getSource()).getScene().getWindow();
                stage.initOwner(ownerStage);
            }

            stage.showAndWait();
            
            return controller;

        } catch (Exception e) {
            tratarErro(e, fxmlPath);
            return null;
        }
    }

    public static void abrirModalSimples(String fxmlPath, String titulo, Event eventOwner) {
        abrirModal(fxmlPath, titulo, null, eventOwner);
    }

    private static Parent loadFxml(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getResource(fxmlPath));
        return loader.load();
    }

    private static URL getResource(String fxmlPath) {
        URL resource = ViewUtils.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalArgumentException("Arquivo FXML não encontrado: " + fxmlPath);
        }
        return resource;
    }

    private static void tratarErro(Exception e, String path) {
        e.printStackTrace();
        AlertUtils.showError("Erro de Interface", "Falha ao carregar: " + path + "\n" + e.getMessage());
    }
}