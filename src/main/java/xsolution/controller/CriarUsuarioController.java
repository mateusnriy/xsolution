package xsolution.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import xsolution.exception.DbException;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;

public class CriarUsuarioController {
  @FXML
  private TextField nomeField;
  @FXML
  private TextField emailField;
  @FXML
  private PasswordField senhaField;
  @FXML
  private PasswordField confirmSenhaField;
  @FXML
  private Button createButton;
  @FXML
  private Hyperlink backToLoginLink;

  private UsuarioService usuarioService;

  public CriarUsuarioController() {
    this.usuarioService = new UsuarioService();
  }

  @FXML
  public void initialize() {
  }

  @FXML
  private void handleCreate(ActionEvent event) {
    String nome = nomeField.getText();
    String email = emailField.getText();
    String senha = senhaField.getText();
    String confirmSenha = confirmSenhaField.getText();

    try {
      if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmSenha.isEmpty()) {
        AlertUtils.showError("Erro no Formulário", "Todos os campos são obrigatórios.");
        return;
      }

      if (!senha.equals(confirmSenha)) {
        AlertUtils.showError("Erro no Formulário", "A nova senha e a confirmação não coincidem.");
        return;
      }

      usuarioService.criarUsuario(nome, email, senha);
      AlertUtils.showInfo("Sucesso", "Conta criada com sucesso! Você já pode fazer o login.");

      handleBack(event);

    } catch (DbException e) {
      AlertUtils.showError("Erro ao Criar Conta", e.getMessage());
    } catch (Exception e) {
      AlertUtils.showError("Erro Inesperado", "Ocorreu um erro: " + e.getMessage());
    }
  }

  @FXML
  private void handleBack(ActionEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/Login.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(scene);
      stage.setTitle("X Solution - Login");

      stage.setResizable(true);
      stage.setMaximized(true);

      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
      AlertUtils.showError("Erro", "Não foi possível voltar ao login.");
    }
  }
}
