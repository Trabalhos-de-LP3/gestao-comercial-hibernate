package dao.hibernate;

import config.TransactionManager;
import dao.VendaDAO;
import model.StatusVenda;
import model.Venda;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;

public class VendaDAOHibernate implements VendaDAO {

    private static final String SELECT_COMPLETO =
            "select distinct v from Venda v "
                    + "left join fetch v.itens i "
                    + "left join fetch i.produto "
                    + "left join fetch v.cliente ";

    @Override
    public void inserir(Venda venda) {
        TransactionManager.executar(() -> TransactionManager.getSession().persist(venda));
    }

    @Override
    public void atualizarStatus(Venda venda) {
        TransactionManager.executar(() -> TransactionManager.getSession().merge(venda));
    }

    @Override
    public Venda buscarPorId(int id) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(SELECT_COMPLETO + "where v.id = :id", Venda.class)
                    .setParameter("id", id)
                    .uniqueResult();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Venda> listarPorCliente(int clienteId) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(
                            SELECT_COMPLETO + "where v.cliente.id = :clienteId order by v.dataVenda desc",
                            Venda.class)
                    .setParameter("clienteId", clienteId)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Venda> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(
                            SELECT_COMPLETO + "where v.dataVenda between :inicio and :fim order by v.dataVenda",
                            Venda.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fim", fim)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Venda> listarPorStatus(StatusVenda status) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(
                            SELECT_COMPLETO + "where v.status = :status order by v.dataVenda desc",
                            Venda.class)
                    .setParameter("status", status)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }
}
