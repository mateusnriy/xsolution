package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import xsolution.exception.DbException;
import xsolution.model.entity.Usuario;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;
import xsolution.utils.Sessao;

public class MeuPerfilController implements Initializable {

  @FXML
  private TextField txtNome;
  @FXML
  private TextField txtEmail;
  @FXML
  private TextField txtSetor;

  @FXML
  private PasswordField txtSenhaAtual;
  @FXML
  private PasswordField txtNovaSenha;
  @FXML
  private PasswordField txtConfirmarSenha;

  private UsuarioService usuarioService;
  private Usuario usuarioLogado;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.usuarioService = new UsuarioService();
    this.usuarioLogado = Sessao.getUsuarioLogado();

    if (usuarioLogado == null) {
      AlertUtils.showError("Erro", "Sessão inválida. Por favor, faça login novamente.");
      return;
    }

    carregarDados();
  }

  private void carregarDados() {
    txtNome.setText(usuarioLogado.getNome());
    txtEmail.setText(usuarioLogado.getEmail());

    if (usuarioLogado.getSetor() != null) {
      txtSetor.setText(usuarioLogado.getSetor().getSigla() + " - " + usuarioLogado.getSetor().getNome());
    } else {
      txtSetor.setText("Não atribuído");
    }
  }

  @FXML
  public void handleSalvar(ActionEvent event) {
    try {
      usuarioLogado.setNome(txtNome.getText());
      usuarioLogado.setEmail(txtEmail.getText());

      String senhaAtual = txtSenhaAtual.getText();
      String novaSenha = txtNovaSenha.getText();
      String confirmacao = txtConfirmarSenha.getText();

      usuarioService.atualizarPerfil(usuarioLogado, senhaAtual, novaSenha, confirmacao);

      Sessao.setUsuarioLogado(usuarioLogado);

      AlertUtils.showInfo("Sucesso", "Perfil atualizado com sucesso!");

      txtSenhaAtual.clear();
      txtNovaSenha.clear();
      txtConfirmarSenha.clear();

    } catch (DbException e) {
      AlertUtils.showError("Erro de Validação", e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      AlertUtils.showError("Erro Crítico", "Ocorreu um erro ao atualizar o perfil: " + e.getMessage());
    }
  }
}