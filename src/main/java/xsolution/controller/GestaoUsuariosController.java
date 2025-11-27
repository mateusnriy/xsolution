package xsolution.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;
import xsolution.utils.FilterHelper;
import xsolution.utils.ViewUtils;

public class GestaoUsuariosController implements Initializable {

  @FXML
  private TextField filterNome;
  @FXML
  private ComboBox<PerfilUsuario> filterPerfil;
  @FXML
  private ComboBox<StatusUsuario> filterStatus;

  @FXML
  private TableView<Usuario> tabelaUsuarios;
  @FXML
  private TableColumn<Usuario, String> colPerfil;
  @FXML
  private TableColumn<Usuario, String> colStatus;
  @FXML
  private TableColumn<Usuario, String> colSetor;

  private UsuarioService usuarioService;
  private ObservableList<Usuario> listaUsuariosView;
  private List<Usuario> todosUsuariosCache;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.usuarioService = new UsuarioService();
    this.listaUsuariosView = FXCollections.observableArrayList();
    this.todosUsuariosCache = new ArrayList<>();

    configurarTabela();
    configurarFiltros();
    carregarDados();
  }

  private void configurarTabela() {
    colPerfil.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPerfil().toString()));
    colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().toString()));

    colSetor.setCellValueFactory(cell -> {
      if (cell.getValue().getSetor() != null) {
        return new SimpleStringProperty(cell.getValue().getSetor().getSigla());
      }
      return new SimpleStringProperty("-");
    });

    tabelaUsuarios.setItems(listaUsuariosView);

    tabelaUsuarios.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && tabelaUsuarios.getSelectionModel().getSelectedItem() != null) {
        handleEditarUsuario(tabelaUsuarios.getSelectionModel().getSelectedItem());
      }
    });
  }

  private void configurarFiltros() {
    filterPerfil.setItems(FXCollections.observableArrayList(PerfilUsuario.values()));
    filterStatus.setItems(FXCollections.observableArrayList(StatusUsuario.values()));
  }

  private void carregarDados() {
    try {
      List<Usuario> lista = usuarioService.listarTodos();
      todosUsuariosCache.clear();
      todosUsuariosCache.addAll(lista);
      listaUsuariosView.setAll(lista);
    } catch (Exception e) {
      AlertUtils.showError("Erro", "Falha ao carregar usuários: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  public void handleFiltrar(ActionEvent event) {
    List<Usuario> filtrados = todosUsuariosCache.stream()
        .filter(u -> FilterHelper.matchString(u.getNome(), filterNome.getText()))
        .filter(u -> FilterHelper.matchEquals(u.getPerfil(), filterPerfil.getValue()))
        .filter(u -> FilterHelper.matchEquals(u.getStatus(), filterStatus.getValue()))
        .collect(Collectors.toList());

    listaUsuariosView.setAll(filtrados);
  }

  @FXML
  public void handleLimparFiltros(ActionEvent event) {
    filterNome.clear();
    filterPerfil.setValue(null);
    filterStatus.setValue(null);
    listaUsuariosView.setAll(todosUsuariosCache);
  }

  private void handleEditarUsuario(Usuario usuarioSelecionado) {
    ModalUsuarioController controller = ViewUtils.abrirModal(
        "/xsolution/view/ModalUsuario.fxml",
        "Editar Usuário - " + usuarioSelecionado.getId(),
        (ctrl) -> ctrl.setUsuario(usuarioSelecionado),
        null);

    if (controller != null && controller.isSalvou()) {
      carregarDados();
    }
  }
}