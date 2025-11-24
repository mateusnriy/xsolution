package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Administrador;
import xsolution.model.entity.Servidor;
import xsolution.model.entity.Setor;
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
        return String.format("S%03d", maxId + 1);
      } else {
        return "S001";
      }
    } catch (SQLException e) {
      throw new DbException("Erro ao gerar ID: " + e.getMessage(), e);
    } finally {
      DB.closeStatement(st);
      DB.closeResults(rs);
    }
  }

  @Override
  public void inserir(Servidor servidor) {
    String sql = "INSERT INTO usuario (idUsuario, nome, email, senha, status, tipoUsuario, idSetor) VALUES (?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement st = null;
    try {
      st = conn.prepareStatement(sql);
      st.setString(1, servidor.getId());
      st.setString(2, servidor.getNome());
      st.setString(3, servidor.getEmail());
      st.setString(4, servidor.getSenhaHash());

      String statusStr = (servidor.getStatus() == StatusUsuario.ATIVO) ? "ATIVO" : "INATIVO";
      st.setString(5, statusStr);

      st.setString(6, servidor.getPerfil().toString());

      if (servidor.getSetor() != null) {
        st.setInt(7, servidor.getSetor().getId());
      } else {
        st.setNull(7, Types.INTEGER);
      }

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
    String sql = "SELECT u.*, s.nome as nome_setor, s.sigla as sigla_setor " +
        "FROM usuario u " +
        "LEFT JOIN Setor s ON u.idSetor = s.idSetor " +
        "WHERE u.email = ?";

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
    String tipoUsuario = rs.getString("tipoUsuario");

    if ("ADMINISTRADOR".equals(tipoUsuario) || idUsuario.startsWith("A")) {
      usuario = new Administrador();
      usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);
    } else if ("TECNICO".equals(tipoUsuario) || idUsuario.startsWith("T")) {
      usuario = new Tecnico();
      usuario.setPerfil(PerfilUsuario.TECNICO);
    } else {
      usuario = new Servidor();
      usuario.setPerfil(PerfilUsuario.COMUM);
    }

    usuario.setId(idUsuario);
    usuario.setNome(rs.getString("nome"));
    usuario.setEmail(rs.getString("email"));
    usuario.setSenhaHash(rs.getString("senha"));

    String statusStr = rs.getString("status");
    usuario.setStatus("ATIVO".equalsIgnoreCase(statusStr) ? StatusUsuario.ATIVO : StatusUsuario.INATIVO);

    int idSetor = rs.getInt("idSetor");
    if (idSetor > 0) {
      Setor s = new Setor();
      s.setId(idSetor);
      try {
        s.setNome(rs.getString("nome_setor"));
        s.setSigla(rs.getString("sigla_setor"));
      } catch (SQLException ex) {
        
      }
      usuario.setSetor(s);
    }

    return usuario;
  }

  @Override
  public List<Usuario> listarTecnicos() {
    List<Usuario> tecnicos = new ArrayList<>();
    String sql = "SELECT * FROM Usuario WHERE idUsuario LIKE 'T%' AND status = 'ATIVO' ORDER BY nome";

    PreparedStatement st = null;
    ResultSet rs = null;

    try {
      st = conn.prepareStatement(sql);
      rs = st.executeQuery();

      while (rs.next()) {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(rs.getString("idUsuario"));
        tecnico.setNome(rs.getString("nome"));
        tecnico.setEmail(rs.getString("email"));
        tecnico.setPerfil(PerfilUsuario.TECNICO);
        tecnico.setStatus(StatusUsuario.ATIVO);

        tecnicos.add(tecnico);
      }

    } catch (SQLException e) {
      throw new DbException("Erro ao buscar técnicos: " + e.getMessage(), e);
    } finally {
      DB.closeResults(rs);
      DB.closeStatement(st);
    }
    return tecnicos;
  }
}