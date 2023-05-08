-- ESEGUIRE UNA SOLA VOLTA QUESTE QUERY
-- PER GENERARE IL DATASABASE cashreg E
-- CREARE LE TABELLE NECESSARIE AL 
-- DATABASE ENGINE PER RENDERE PERSISTENTI
-- GLI OGGETTI

create database cashreg encoding='UTF-8';

create table scontrini(
	id int primary key,
	tipo varchar(32),
	stato varchar(32),
	intestazione varchar(4096),
	piedipagina varchar(4096),
	dataEmissione date,
	oraEmissione time
);


create table righe(
	idRiga int primary key,
	idScontrino int,
	prezzoUnitario numeric(11,2),
	sconto numeric(11,2),
	iva numeric(11,2),
	quantita numeric(11,2),
	descrizione varchar(1024)
);

alter table righe add foreign key (idScontrino) references scontrini(id);
