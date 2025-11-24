package xsolution.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ScreenUtils {
  public static void changeScreen(ActionEvent event, String fxmlPath, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(ScreenUtils.class.getResource(fxmlPath));
      Parent newRoot = loader.load();

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      Scene currentScene = stage.getScene();

      currentScene.setRoot(newRoot);
      stage.setTitle(title);

      if (!stage.isMaximized()) {
        stage.setMaximized(true);
      }

    } catch (IOException e) {
      e.printStackTrace();
      AlertUtils.showError("Erro de Navegação", "Não foi possível carregar a tela: " + fxmlPath);
    }
  }
}