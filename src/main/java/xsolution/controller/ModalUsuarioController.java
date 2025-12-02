package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import xsolution.model.entity.Setor;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;
import xsolution.service.SetorService;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;

public class ModalUsuarioController implements Initializable {

  @FXML
  private TextField txtNome;
  @FXML
  private TextField txtEmail;
  @FXML
  private ComboBox<PerfilUsuario> cbPerfil;
  @FXML
  private ComboBox<StatusUsuario> cbStatus;
  @FXML
  private ComboBox<Setor> cbSetor;

  private UsuarioService service;
  private SetorService setorService;
  private Usuario usuarioAtual;
  private boolean salvou = false;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.service = new UsuarioService();
    this.setorService = new SetorService();

    cbPerfil.setItems(FXCollections.observableArrayList(PerfilUsuario.values()));
    cbStatus.setItems(FXCollections.observableArrayList(StatusUsuario.values()));

    configurarComboSetor();
  }

  private void configurarComboSetor() {
    cbSetor.setConverter(new StringConverter<Setor>() {
      @Override
      public String toString(Setor setor) {
        if (setor == null)
          return null;
        return setor.getSigla() + " - " + setor.getNome();
      }

      @Override
      public Setor fromString(String string) {
        return null;
      }
    });

    try {
      cbSetor.setItems(FXCollections.observableArrayList(setorService.listarTodos()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setUsuario(Usuario usuario) {
    this.usuarioAtual = usuario;
    if (usuario != null) {
      txtNome.setText(usuario.getNome());
      txtEmail.setText(usuario.getEmail());
      cbPerfil.setValue(usuario.getPerfil());
      cbStatus.setValue(usuario.getStatus());
      cbSetor.setValue(usuario.getSetor());
    }
  }

  @FXML
  public void handleSalvar(ActionEvent event) {
    try {
      usuarioAtual.setNome(txtNome.getText());
      usuarioAtual.setEmail(txtEmail.getText());
      usuarioAtual.setPerfil(cbPerfil.getValue());
      usuarioAtual.setStatus(cbStatus.getValue());
      usuarioAtual.setSetor(cbSetor.getValue());

      service.atualizarUsuario(usuarioAtual);

      AlertUtils.showInfo("Sucesso", "Usuário atualizado com sucesso.");
      this.salvou = true;
      closeWindow();

    } catch (Exception e) {
      AlertUtils.showError("Erro", e.getMessage());
    }
  }

  @FXML
  public void handleCancelar(ActionEvent event) {
    closeWindow();
  }

  private void closeWindow() {
    Stage stage = (Stage) txtNome.getScene().getWindow();
    stage.close();
  }

  public boolean isSalvou() {
    return salvou;
  }
}