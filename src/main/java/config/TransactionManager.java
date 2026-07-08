package config;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TransactionManager {

    private static final ThreadLocal<Session> SESSAO_ATUAL = new ThreadLocal<>();

    private TransactionManager() {
    }

    public static Session getSession() {
        Session sessao = SESSAO_ATUAL.get();
        if (sessao != null) {
            return sessao;
        }
        return HibernateUtil.getSessionFactory().openSession();
    }

    public static boolean transacaoAtiva() {
        return SESSAO_ATUAL.get() != null;
    }

    public static void liberar(Session sessao) {
        if (!transacaoAtiva() && sessao != null && sessao.isOpen()) {
            sessao.close();
        }
    }

    public static void executar(Runnable acao) {
        if (transacaoAtiva()) {
            acao.run();
            return;
        }

        Session sessao = HibernateUtil.getSessionFactory().openSession();
        Transaction transacao = null;
        try {
            transacao = sessao.beginTransaction();
            SESSAO_ATUAL.set(sessao);
            acao.run();
            transacao.commit();
        } catch (RuntimeException | Error e) {
            if (transacao != null && transacao.isActive()) {
                transacao.rollback();
            }
            throw e;
        } finally {
            SESSAO_ATUAL.remove();
            if (sessao.isOpen()) {
                sessao.close();
            }
        }
    }
}
