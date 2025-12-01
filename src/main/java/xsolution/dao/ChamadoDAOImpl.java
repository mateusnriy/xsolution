package xsolution.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Chamado;
import xsolution.model.entity.Equipamento;
import xsolution.model.entity.Servidor;
import xsolution.model.entity.Tecnico;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.StatusChamado;
import xsolution.model.enums.TipoEquipamento;

public class ChamadoDAOImpl implements ChamadoDAO {

    @Override
    public List<Chamado> listarChamados() {
        List<Chamado> chamados = new ArrayList<>();
        String sql = "SELECT " +
                "c.idChamado, c.protocolo, c.titulo, c.descricao, c.status, c.dataAbertura, c.dataFechamento, " +
                "u_criador.idUsuario AS id_solicitante, u_criador.nome AS nome_solicitante, u_criador.email AS email_solicitante, "
                +
                "u_tec.idUsuario AS id_tecnico, u_tec.nome AS nome_tecnico, " +
                "e.idEquipamento, e.numPatrimonio, e.tipo AS tipo_equipamento " +
                "FROM Chamado c " +
                "LEFT JOIN Usuario u_criador ON c.idUsuarioCriador = u_criador.idUsuario " +
                "LEFT JOIN Usuario u_tec ON c.idUsuarioResponsavel = u_tec.idUsuario " +
                "LEFT JOIN Equipamento e ON c.idEquipamento = e.idEquipamento " +
                "ORDER BY c.dataAbertura DESC";

        try (Connection conn = DB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Chamado chamado = mapeamentoChamado(rs);
                chamados.add(chamado);
            }

        } catch (SQLException e) {
            throw new DbException("Erro ao buscar chamados: " + e.getMessage(), e);
        }

        return chamados;
    }

    @Override
    public List<Chamado> listarPorSolicitante(Usuario solicitante) {
        List<Chamado> chamados = new ArrayList<>();
        String sql = "SELECT " +
                "c.idChamado, c.protocolo, c.titulo, c.descricao, c.status, c.dataAbertura, c.dataFechamento, " +
                "u_criador.idUsuario AS id_solicitante, u_criador.nome AS nome_solicitante, u_criador.email AS email_solicitante, " +
                "u_tec.idUsuario AS id_tecnico, u_tec.nome AS nome_tecnico, " +
                "e.idEquipamento, e.numPatrimonio, e.tipo AS tipo_equipamento " +
                "FROM Chamado c " +
                "LEFT JOIN Usuario u_criador ON c.idUsuarioCriador = u_criador.idUsuario " +
                "LEFT JOIN Usuario u_tec ON c.idUsuarioResponsavel = u_tec.idUsuario " +
                "LEFT JOIN Equipamento e ON c.idEquipamento = e.idEquipamento " +
                "WHERE c.idUsuarioCriador = ? " +
                "ORDER BY c.dataAbertura DESC";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String idSolicitante = solicitante.getId();
            pstmt.setString(1, idSolicitante);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Chamado chamado = mapeamentoChamado(rs);
                    chamados.add(chamado);
                }
            }
        } catch (SQLException e) {
            throw new DbException("Erro ao buscar chamados por solicitante: " + e.getMessage(), e);
        }
        return chamados;
    }

    public List<Chamado> listarPorFiltros(String titulo, StatusChamado status, java.sql.Timestamp dataAbertura) {
        List<Chamado> chamados = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.idChamado, c.protocolo, c.titulo, c.descricao, c.status, c.dataAbertura, c.dataFechamento, " +
                "u_criador.idUsuario AS id_solicitante, u_criador.nome AS nome_solicitante, u_criador.email AS email_solicitante, " +
                "u_tec.idUsuario AS id_tecnico, u_tec.nome AS nome_tecnico, " +
                "e.idEquipamento, e.numPatrimonio, e.tipo AS tipo_equipamento " +
                "FROM Chamado c " +
                "LEFT JOIN Usuario u_criador ON c.idUsuarioCriador = u_criador.idUsuario " +
                "LEFT JOIN Usuario u_tec ON c.idUsuarioResponsavel = u_tec.idUsuario " +
                "LEFT JOIN Equipamento e ON c.idEquipamento = e.idEquipamento " +
                "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (titulo != null && !titulo.isBlank()) {
            sql.append(" AND c.titulo LIKE ?");
            params.add("%" + titulo + "%");
        }

        if (status != null) {
            sql.append(" AND c.status = ?");
            params.add(status.name());
        }

        if (dataAbertura != null) {
            sql.append(" AND DATE(c.dataAbertura) = DATE(?)");
            params.add(dataAbertura);
        }

        sql.append(" ORDER BY c.dataAbertura DESC");

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) param);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Chamado chamado = mapeamentoChamado(rs);
                    chamados.add(chamado);
                }
            }
        } catch (SQLException e) {
            throw new DbException("Erro ao buscar chamados por filtros: " + e.getMessage(), e);
        }

        return chamados;
    }

    @Override
    public void criar(Chamado chamado) {
        String sql = "INSERT INTO Chamado (protocolo, titulo, descricao, status, dataAbertura, idUsuarioCriador, idEquipamento) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, chamado.getProtocolo());
            pstmt.setString(2, chamado.getTitulo());
            pstmt.setString(3, chamado.getDescricao());
            pstmt.setString(4, chamado.getStatus().name());
            pstmt.setTimestamp(5, Timestamp.valueOf(chamado.getDataAbertura()));

            if (chamado.getSolicitante() != null) {
                pstmt.setString(6, chamado.getSolicitante().getId());
            } else {
                throw new SQLException("É obrigatório informar o Solicitante para abrir um chamado.");
            }

            if (chamado.getEquipamento() != null) {
                pstmt.setInt(7, chamado.getEquipamento().getId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    chamado.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new DbException("Erro ao criar chamado: " + e.getMessage(), e);
        }
    }
      
    @Override
    public void atualizar(Chamado chamado) {
        String sql = "UPDATE Chamado SET status = ?, idUsuarioResponsavel = ?, dataFechamento = ? WHERE idChamado = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, chamado.getStatus().toString());
            
            if (chamado.getTecnicoResponsavel() != null) {
                pstmt.setString(2, chamado.getTecnicoResponsavel().getId());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            if (chamado.getDataFechamento() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(chamado.getDataFechamento()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }

            pstmt.setInt(4, chamado.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DbException("Erro ao atualizar chamado: " + e.getMessage(), e);
        }
    }

    private Chamado mapeamentoChamado(ResultSet rs) throws SQLException {
        Chamado chamado = new Chamado();
        chamado.setId(rs.getInt("idChamado"));
        chamado.setProtocolo(rs.getString("protocolo"));
        chamado.setTitulo(rs.getString("titulo"));
        chamado.setDescricao(rs.getString("descricao"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                chamado.setStatus(StatusChamado.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                System.err.println("Status desconhecido no banco: " + statusStr);
            }
        }

        Timestamp tsAbertura = rs.getTimestamp("dataAbertura");
        if (tsAbertura != null)
            chamado.setDataAbertura(tsAbertura.toLocalDateTime());

        Timestamp tsFechamento = rs.getTimestamp("dataFechamento");
        if (tsFechamento != null)
            chamado.setDataFechamento(tsFechamento.toLocalDateTime());

        String idSolicitante = rs.getString("id_solicitante");
        if (idSolicitante != null && !idSolicitante.isBlank()) {

            Servidor solicitante = new Servidor();
            solicitante.setId(idSolicitante);
            solicitante.setNome(rs.getString("nome_solicitante"));
            solicitante.setEmail(rs.getString("email_solicitante"));
            chamado.setSolicitante(solicitante);
        }

        String idTecnico = rs.getString("id_tecnico");
        if (idTecnico != null && !idTecnico.isBlank()) {
            Tecnico tecnico = new Tecnico();
            tecnico.setId(idTecnico);
            tecnico.setNome(rs.getString("nome_tecnico"));
            chamado.setTecnicoResponsavel(tecnico);
        }

        int idEquipamento = rs.getInt("idEquipamento");
        if (idEquipamento > 0) {
            Equipamento equipamento = new Equipamento();
            equipamento.setId(idEquipamento);
            equipamento.setNumPatrimonio(rs.getString("numPatrimonio"));

            String tipoStr = rs.getString("tipo_equipamento");
            if (tipoStr != null) {
                try {
                    equipamento.setTipo(TipoEquipamento.valueOf(tipoStr));
                } catch (IllegalArgumentException e) {
                    System.err.println("Tipo Equipamento desconhecido: " + tipoStr);
                }
            }
            chamado.setEquipamento(equipamento);
        }

        return chamado;
    }

}
