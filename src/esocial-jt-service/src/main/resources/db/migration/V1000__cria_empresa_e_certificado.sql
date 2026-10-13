-- Faixa V1000+ reservada ao fork multiempresa, para nao colidir com migrations do upstream.

create table EST_EMPRESA (
       COD_EMPRESA integer not null,
       TXT_CNPJ varchar(14) not null,
       TXT_RAZAO_SOCIAL varchar(200) not null,
       COD_TP_INSC smallint not null default 1,
       COD_TP_INSC_TRANSMISSOR smallint,
       TXT_NR_INSC_TRANSMISSOR varchar(14),
       TXT_AMBIENTE varchar(20),
       IND_ATIVO boolean not null default true,
       DTA_CRIACAO timestamp not null default current_timestamp,
       DTA_ATUALIZACAO timestamp
);
alter table EST_EMPRESA add constraint EST_EMPRESA_PK primary key (COD_EMPRESA);
alter table EST_EMPRESA add constraint EST_EMPRESA_UK01 unique (TXT_CNPJ);

create table EST_CERTIFICADO (
       COD_CERTIFICADO integer not null,
       COD_EMPRESA integer not null,
       BIN_ARQUIVO bytea not null,
       TXT_SENHA_CRIPTOGRAFADA varchar(500) not null,
       TXT_TIPO varchar(20) not null default 'pkcs12',
       TXT_ALIAS varchar(100),
       TXT_CNPJ_CERTIFICADO varchar(14),
       DTA_VALIDADE timestamp,
       IND_ATIVO boolean not null default true,
       DTA_CRIACAO timestamp not null default current_timestamp
);
alter table EST_CERTIFICADO add constraint EST_CERTIFICADO_PK primary key (COD_CERTIFICADO);
alter table EST_CERTIFICADO add constraint EST_CERTIFICADO_FK01 foreign key (COD_EMPRESA)
       references EST_EMPRESA (COD_EMPRESA);
create index EST_CERTIFICADO_IDX01 on EST_CERTIFICADO (COD_EMPRESA, IND_ATIVO);

create sequence SEQ_EMPRESA_ID minvalue 1 maxvalue 99999999 increment by 1 start with 1 cache 1 no cycle;
create sequence SEQ_CERTIFICADO_ID minvalue 1 maxvalue 99999999 increment by 1 start with 1 cache 1 no cycle;

-- NOT NULL exige banco zerado: nao ha migracao de dados legados.
alter table EST_OCORRENCIA add COD_EMPRESA integer not null;
alter table EST_LOTE add COD_EMPRESA integer not null;

alter table EST_OCORRENCIA add constraint EST_OCORRENCIA_FK_EMPRESA foreign key (COD_EMPRESA)
       references EST_EMPRESA (COD_EMPRESA);
alter table EST_LOTE add constraint EST_LOTE_FK_EMPRESA foreign key (COD_EMPRESA)
       references EST_EMPRESA (COD_EMPRESA);

create index EST_OCORRENCIA_IDX_EMPRESA on EST_OCORRENCIA (COD_EMPRESA);
create index EST_LOTE_IDX_EMPRESA on EST_LOTE (COD_EMPRESA);
