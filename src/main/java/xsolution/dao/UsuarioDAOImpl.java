package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Administrador;
import xsolution.model.entity.Servidor;
import xsolution.model.entity.Tecnico;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;

public class UsuarioDAOImpl implements UsuarioDAO {

  private Connection conn;

  public UsuarioDAOImpl(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String gerarProximoIdServidor() {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "SELECT MAX(CAST(SUBSTRING(idUsuario FROM 2) AS INTEGER)) FROM usuario WHERE idUsuario LIKE 'S%'";

    try {
      st = conn.prepareStatement(sql);
      rs = st.executeQuery();

      if (rs.next()) {
        int maxId = rs.getInt(1);
        int nextId = maxId + 1;
        return String.format("S%03d", nextId);
      } else {
        return "S001";
      }

    } catch (SQLException e) {
      throw new DbException("Erro ao gerar novo ID de servidor: " + e.getMessage(), e);
    } finally {
      DB.closeStatement(st);
      DB.closeResults(rs);
    }
  }

  @Override
  public void inserir(Servidor servidor) {
    PreparedStatement st = null;
    String sql = "INSERT INTO usuario (idUsuario, nome, email, senha, status) "
        + "VALUES (?, ?, ?, ?, ?)";

    try {
      st = conn.prepareStatement(sql);
      st.setString(1, servidor.getId());
      st.setString(2, servidor.getNome());
      st.setString(3, servidor.getEmail());
      st.setString(4, servidor.getSenhaHash());

      String statusString = (servidor.getStatus() == StatusUsuario.ATIVO) ? "Ativo" : "Inativo";

      st.setString(5, statusString);
      st.executeUpdate();

    } catch (SQLException e) {
      throw new DbException("Erro ao inserir usuário: " + e.getMessage(), e);
    } finally {
      DB.closeStatement(st);
    }
  }

  @Override
  public Usuario buscarPorEmail(String email) {
    PreparedStatement st = null;
    ResultSet rs = null;
    String sql = "SELECT * FROM usuario WHERE email = ?";

    try {
      st = conn.prepareStatement(sql);
      st.setString(1, email);
      rs = st.executeQuery();

      if (rs.next()) {
        return instanciarUsuario(rs);
      }
      return null;

    } catch (SQLException e) {
      throw new DbException("Erro ao buscar usuário por email: " + e.getMessage(), e);
    } finally {
      DB.closeStatement(st);
      DB.closeResults(rs);
    }
  }

  private Usuario instanciarUsuario(ResultSet rs) throws SQLException {
    Usuario usuario;

    String idUsuario = rs.getString("idUsuario");

    if (idUsuario.toUpperCase().startsWith("A")) {
      usuario = new Administrador();
      usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);

    } else if (idUsuario.toUpperCase().startsWith("T")) {
      usuario = new Tecnico();
      usuario.setPerfil(PerfilUsuario.TECNICO);

    } else {
      Servidor servidor = new Servidor();
      servidor.setLotacao(rs.getString("idSetor")); // Precisa ser ajustado depois
      usuario = servidor;
      usuario.setPerfil(PerfilUsuario.COMUM);
    }

    usuario.setId(rs.getString("idUsuario"));
    usuario.setNome(rs.getString("nome"));
    usuario.setEmail(rs.getString("email"));
    usuario.setSenhaHash(rs.getString("senha"));

    String statusStr = rs.getString("status");
    StatusUsuario status = "Ativo".equalsIgnoreCase(statusStr) ? StatusUsuario.ATIVO : StatusUsuario.INATIVO;

    usuario.setStatus(status);

    return usuario;
  }
}