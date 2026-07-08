# Sistema de Gestão Comercial — Versão Hibernate

Gestão de clientes, produtos, estoque e vendas em Java, com persistência em MySQL via
**Hibernate** (ORM). Faz parte de um trabalho com duas versões (JDBC e Hibernate) que
compartilham o mesmo domínio e as mesmas regras de negócio.

## Stack

- Java 21 (Maven)
- Hibernate ORM 6.6 (`hibernate-core`) + `mysql-connector-j`
- MySQL 8.4 em container Docker

## Como rodar

1. **Banco**: na pasta `gestao-comercial-db/` (na raiz do workspace), rode `docker compose up -d`.
   Cria o banco `gestao_comercial` (usuário `app`/`app`, porta 3306) com tabelas e dados de exemplo.
2. **Projeto**: no Eclipse, **File → Import → Maven → Existing Maven Projects** apontando para esta pasta
   (o m2e baixa o Hibernate e as dependências transitivas automaticamente).
3. Rode a classe `Main` (Run As → Java Application) e use o menu no console.

## Estrutura

```
src/main/java/
├── exception/      NegocioException, EstoqueInsuficienteException
├── model/          entidades anotadas com JPA (@Entity, @OneToMany, @ManyToOne, ...)
├── dao/            interfaces (contrato de persistência)
├── dao/hibernate/  implementação com Session/HQL e join fetch
├── service/        regras de negócio e validações
├── config/         HibernateUtil (SessionFactory), TransactionManager
└── Main.java       menu textual
src/main/resources/
└── hibernate.cfg.xml   dialeto, conexão e mapeamento das entidades
```

## Detalhes de mapeamento

- Acesso por **campo** (anotações nos atributos) para o Hibernate não disparar os setters
  com validação de regra ao carregar.
- `hbm2ddl.auto=none`: usa o esquema criado pelo Docker (mesmo banco da versão JDBC).
- `@OneToMany(cascade=ALL, orphanRemoval=true)` em `Venda.itens`; `@Enumerated(STRING)` no status.

## Regras de negócio

- Cliente/produto inativo não participa de vendas.
- Não se vende acima do estoque disponível.
- Confirmar venda dá baixa no estoque; cancelar estorna — em **transação única**.
- Total da venda calculado pelos itens; preço unitário registrado no momento da venda.

## Documentação

- [`docs/ROTEIRO_DEMONSTRACAO.md`](docs/ROTEIRO_DEMONSTRACAO.md) — passo a passo da demo.
- [`docs/EVIDENCIAS.md`](docs/EVIDENCIAS.md) — fluxos verificados em execução real (com SQL do ORM).
- [`docs/RELATORIO_COMPARATIVO.md`](docs/RELATORIO_COMPARATIVO.md) — JDBC × Hibernate.
