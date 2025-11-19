package xsolution.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import xsolution.exception.DbException;
import xsolution.model.entity.Usuario;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;

public class LoginController {
  @FXML
  private TextField emailField;
  @FXML
  private PasswordField senhaField;
  @FXML
  private Button loginButton;

  private UsuarioService usuarioService;

  public LoginController() {
    this.usuarioService = new UsuarioService();
  }

  @FXML
  private void handleLogin(ActionEvent event) {
    String email = emailField.getText();
    String senha = senhaField.getText();

    if (email.isEmpty() || senha.isEmpty()) {
      AlertUtils.showError("Erro de Login", "E-mail e senha são obrigatórios.");
      return;
    }

    try {
      Usuario usuarioAutenticado = usuarioService.autenticar(email, senha);

      if (usuarioAutenticado != null) {
        usuarioAutenticado.getPerfil();
        navigateTo(event, "/xsolution/view/MainDashboard.fxml", "X Solution - Dashboard");

      } else {
        AlertUtils.showError("Login Falhou", "Usuário não encontrado.");
      }
    } catch (DbException e) {
      AlertUtils.showError("Login Falhou", e.getMessage());
    }
  }

  @FXML
  private void handleRecover(ActionEvent event) {
    navigateTo(event, "/xsolution/view/RecuperarSenha.fxml", "X Solution - Recuperar Senha");
  }

  @FXML
  private void handleCreate(ActionEvent event) {
    navigateTo(event, "/xsolution/view/CriarConta.fxml", "X Solution - Criar Conta");
  }

  private void navigateTo(ActionEvent event, String fxmlPath, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent root = loader.load();
      Scene scene = new Scene(root);

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(scene);
      stage.setTitle(title);

      stage.setResizable(true);
      stage.setMaximized(true);

      // if (fxmlPath.contains("MainDashboard")) {
      // stage.setFullScreen(true); Aqui serve para deixar modo full tela cheia
      // }

      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
      AlertUtils.showError("Erro de Navegação", "Não foi possível carregar a tela: " + fxmlPath);
    }
  }
}
