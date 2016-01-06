drop table Customer if exists;
drop table Customer_tags if exists;

create table customer (id bigint auto_increment primary key, 
	firstName varchar(255), 
	lastName varchar(255), 
	version integer, 
	primary key (id)
);
create table customer_tags (
	customer_id bigint not null, 
	tags varchar(255)
);

alter table customer_tags add constraint fk_tags_customer_id
	foreign key (customer_id) 
	references customer;

insert into customer (id, firstName, lastName, version) values (null, 'Hugh', 'Jackman', 999);
--INSERT INTO customer (id, firstName, lastName, version) VALUES (default, 'Hugh', 'Jackman', 1);
--INSERT INTO customer (id, firstName, lastName) VALUES (null, 'Vin', 'Diesel');