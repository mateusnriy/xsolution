package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Equipamento;
import xsolution.model.enums.StatusEquipamento;
import xsolution.model.enums.TipoEquipamento;

public class EquipamentoDAOImpl implements EquipamentoDAO {

    @Override
    public Equipamento buscarPorPatrimonio(String numPatrimonio) {
        String sql = "SELECT * FROM Equipamento WHERE numPatrimonio = ?";
        Equipamento equipamento = null;

        try (Connection conn = DB.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setString(1, numPatrimonio);
            
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    equipamento = new Equipamento();
                    equipamento.setId(rs.getInt("idEquipamento"));
                    equipamento.setNumPatrimonio(rs.getString("numPatrimonio"));
                    equipamento.setMarca(rs.getString("marca"));
                    equipamento.setModelo(rs.getString("modelo"));
                    
                    // Convertendo Enums (com tratamento de erro básico)
                    try {
                        equipamento.setStatus(StatusEquipamento.valueOf(rs.getString("status")));
                        equipamento.setTipo(TipoEquipamento.valueOf(rs.getString("tipo")));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Erro de conversão de Enum: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new DbException("Erro ao buscar equipamento: " + e.getMessage(), e);
        }
        return equipamento;
    }
}