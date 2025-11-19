package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import xsolution.model.entity.Tecnico;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;
import xsolution.util.ConnectionFactory;

public class UsuarioDAO {

    // Busca todos os usuários com perfil TÉCNICO para preencher o ComboBox
    public List<Usuario> findAllTecnicos() {
        List<Usuario> tecnicos = new ArrayList<>();
        String sql = "SELECT * FROM Usuario WHERE perfil = 'TECNICO' AND status = 'ATIVO'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Tecnico tecnico = new Tecnico();
                tecnico.setId(rs.getInt("idUsuario"));
                tecnico.setNome(rs.getString("nome"));
                tecnico.setEmail(rs.getString("email"));
                // Precisamos definir o perfil para a lógica de validação funcionar
                tecnico.setPerfil(PerfilUsuario.TECNICO);
                tecnico.setStatus(StatusUsuario.ATIVO);
                
                tecnicos.add(tecnico);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar técnicos: " + e.getMessage(), e);
        }
        return tecnicos;
    }
}