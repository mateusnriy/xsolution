package xsolution.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertUtils {
  public static void showAlert(AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void showError(String title, String message) {
    showAlert(AlertType.ERROR, title, message);
  }

  public static void showInfo(String title, String message) {
    showAlert(AlertType.INFORMATION, title, message);
  }
}