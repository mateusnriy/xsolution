package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Equipamento;
import xsolution.model.entity.Setor;
import xsolution.model.enums.StatusEquipamento;
import xsolution.model.enums.TipoEquipamento;

public class EquipamentoDAOImpl implements EquipamentoDAO {

    @Override
    public void salvar(Equipamento e) {
        String sql = "INSERT INTO Equipamento (numPatrimonio, numSerie, marca, modelo, tipo, status, idSetor, data_aquisicao, dataCriacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement(sql);
            
            st.setString(1, e.getNumPatrimonio());
            st.setString(2, e.getNumSerie());
            st.setString(3, e.getMarca());
            st.setString(4, e.getModelo());
            st.setString(5, e.getTipo().toString());
            st.setString(6, e.getStatus().name());
            
            if (e.getSetor() != null && e.getSetor().getId() != null) {
                st.setInt(7, e.getSetor().getId());
            } else {
                st.setNull(7, Types.INTEGER);
            }
            
            if (e.getDataAquisicao() != null) {
                st.setDate(8, java.sql.Date.valueOf(e.getDataAquisicao()));
            } else {
                st.setNull(8, Types.DATE);
            }
            
            if (e.getDataCriacao() != null) {
                st.setTimestamp(9, Timestamp.valueOf(e.getDataCriacao()));
            } else {
                st.setTimestamp(9, Timestamp.valueOf(java.time.LocalDateTime.now()));
            }

            st.executeUpdate();
        } catch (SQLException x) {
            throw new DbException("Erro ao salvar equipamento: " + x.getMessage(), x);
        } finally {
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    @Override
    public void atualizar(Equipamento e) {
        String sql = "UPDATE Equipamento SET numPatrimonio=?, numSerie=?, marca=?, modelo=?, tipo=?, status=?, idSetor=?, data_aquisicao=? WHERE idEquipamento=?";
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement(sql);
            
            st.setString(1, e.getNumPatrimonio());
            st.setString(2, e.getNumSerie());
            st.setString(3, e.getMarca());
            st.setString(4, e.getModelo());
            st.setString(5, e.getTipo().toString());
            st.setString(6, e.getStatus().name());
            
            if (e.getSetor() != null && e.getSetor().getId() != null) {
                st.setInt(7, e.getSetor().getId());
            } else {
                st.setNull(7, Types.INTEGER);
            }
            
            if (e.getDataAquisicao() != null) {
                st.setDate(8, java.sql.Date.valueOf(e.getDataAquisicao()));
            } else {
                st.setNull(8, Types.DATE);
            }
            
            st.setInt(9, e.getId());

            st.executeUpdate();
        } catch (SQLException x) {
            throw new DbException("Erro ao atualizar equipamento: " + x.getMessage(), x);
        } finally {
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM Equipamento WHERE idEquipamento = ?";
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement(sql);
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException x) {
            throw new DbException("Erro ao deletar equipamento: " + x.getMessage(), x);
        } finally {
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    @Override
    public List<Equipamento> listarTodos() {
        // JOIN IMPORTANTE: Traz os dados do Setor junto com o equipamento
        String sql = "SELECT e.*, s.nome AS setor_nome, s.sigla AS setor_sigla " +
                     "FROM Equipamento e " +
                     "LEFT JOIN Setor s ON e.idSetor = s.idSetor " +
                     "ORDER BY e.marca, e.modelo";
        
        List<Equipamento> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            while (rs.next()) {
                lista.add(instanciarEquipamento(rs));
            }
        } catch (SQLException x) {
            throw new DbException("Erro ao listar equipamentos: " + x.getMessage(), x);
        } finally {
            DB.closeResults(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
        return lista;
    }

    @Override
    public Equipamento buscarPorPatrimonio(String numPatrimonio) {
        String sql = "SELECT e.*, s.nome AS setor_nome, s.sigla AS setor_sigla " +
                     "FROM Equipamento e " +
                     "LEFT JOIN Setor s ON e.idSetor = s.idSetor " +
                     "WHERE e.numPatrimonio = ?";
        
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement(sql);
            st.setString(1, numPatrimonio);
            rs = st.executeQuery();
            if (rs.next()) {
                return instanciarEquipamento(rs);
            }
            return null;
        } catch (SQLException x) {
            throw new DbException("Erro ao buscar equipamento: " + x.getMessage(), x);
        } finally {
            DB.closeResults(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    @Override
    public Equipamento buscarPorId(int id) {
        String sql = "SELECT e.*, s.nome AS setor_nome, s.sigla AS setor_sigla " +
                     "FROM Equipamento e " +
                     "LEFT JOIN Setor s ON e.idSetor = s.idSetor " +
                     "WHERE e.idEquipamento = ?";
        
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement(sql);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                return instanciarEquipamento(rs);
            }
            return null;
        } catch (SQLException x) {
            throw new DbException("Erro ao buscar equipamento por ID: " + x.getMessage(), x);
        } finally {
            DB.closeResults(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }

    private Equipamento instanciarEquipamento(ResultSet rs) throws SQLException {
        Equipamento e = new Equipamento();
        e.setId(rs.getInt("idEquipamento"));
        e.setNumPatrimonio(rs.getString("numPatrimonio"));
        e.setNumSerie(rs.getString("numSerie"));
        e.setMarca(rs.getString("marca"));
        e.setModelo(rs.getString("modelo"));
        
        java.sql.Date dtAquisicao = rs.getDate("data_aquisicao");
        if (dtAquisicao != null) {
            e.setDataAquisicao(dtAquisicao.toLocalDate());
        }
        
        Timestamp tsCriacao = rs.getTimestamp("dataCriacao");
        if (tsCriacao != null) {
            e.setDataCriacao(tsCriacao.toLocalDateTime());
        }
        
        try {
            String tipoStr = rs.getString("tipo");
            if (tipoStr != null) e.setTipo(TipoEquipamento.valueOf(tipoStr));
            
            String statusStr = rs.getString("status");
            if (statusStr != null) e.setStatus(StatusEquipamento.valueOf(statusStr));
            
            int idSetor = rs.getInt("idSetor");
            if (!rs.wasNull() && idSetor > 0) {
                Setor setor = new Setor();
                setor.setId(idSetor);
                try {
                    setor.setNome(rs.getString("setor_nome"));
                    setor.setSigla(rs.getString("setor_sigla"));
                } catch (SQLException ex) {
                    // Ignora se as colunas do join não existirem
                }
                e.setSetor(setor);
            }
            
        } catch (IllegalArgumentException ex) {
            System.err.println("Erro de conversão de dados: " + ex.getMessage());
        }
        
        return e;
    }
}