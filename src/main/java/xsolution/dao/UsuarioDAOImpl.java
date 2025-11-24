package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    String sql = "SELECT nextval('seq_servidor_id')"; 
    PreparedStatement st = null;
    ResultSet rs = null;

    try {
      st = conn.prepareStatement(sql);
      rs = st.executeQuery();

      if (rs.next()) {
        long nextId = rs.getLong(1);
        return String.format("S%03d", nextId);
      } else {
        throw new DbException("Erro ao gerar ID: A sequence do banco não retornou valor.");
      }

    } catch (SQLException e) {
      throw new DbException("Erro ao gerar novo ID de servidor: " + e.getMessage(), e);
    } finally {
      DB.closeResults(rs);
      DB.closeStatement(st);
    }
  }

  @Override
  public void inserir(Servidor servidor) {
    PreparedStatement st = null;
    
    String sql = "INSERT INTO usuario (idUsuario, nome, email, senha, status, tipoUsuario) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try {
      st = conn.prepareStatement(sql);
      st.setString(1, servidor.getId());
      st.setString(2, servidor.getNome());
      st.setString(3, servidor.getEmail());
      st.setString(4, servidor.getSenhaHash());

      String statusString = (servidor.getStatus() == StatusUsuario.ATIVO) ? "ATIVO" : "INATIVO";
      st.setString(5, statusString);

      if (servidor.getPerfil() != null) {
          st.setString(6, servidor.getPerfil().toString()); 
      } else {
          st.setString(6, PerfilUsuario.COMUM.toString());
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
    
    String tipoUsuarioDb = rs.getString("tipoUsuario"); 

    if (idUsuario.toUpperCase().startsWith("A") || "ADMINISTRADOR".equals(tipoUsuarioDb)) {
      usuario = new Administrador();
      usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);

    } else if (idUsuario.toUpperCase().startsWith("T") || "TECNICO".equals(tipoUsuarioDb)) {
      usuario = new Tecnico();
      usuario.setPerfil(PerfilUsuario.TECNICO);

    } else {
      Servidor servidor = new Servidor();

      int idSetor = rs.getInt("idSetor");
      servidor.setLotacao(String.valueOf(idSetor)); 
      usuario = servidor;
      usuario.setPerfil(PerfilUsuario.COMUM);
    }

    usuario.setId(rs.getString("idUsuario"));
    usuario.setNome(rs.getString("nome"));
    usuario.setEmail(rs.getString("email"));
    usuario.setSenhaHash(rs.getString("senha"));

    String statusStr = rs.getString("status");
    StatusUsuario status = "ATIVO".equalsIgnoreCase(statusStr) ? StatusUsuario.ATIVO : StatusUsuario.INATIVO;

    usuario.setStatus(status);

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