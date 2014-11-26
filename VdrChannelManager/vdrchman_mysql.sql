create database vdrchman character set utf8 collate utf8_unicode_ci;
use vdrchman;
create user 'vdrchman'@'localhost' identified by 'vdrchman';
grant all privileges on vdrchman.* to 'vdrchman'@'localhost';

create table tuser (
	id int(11) not null primary key auto_increment,
	name varchar(100) not null,
	password varchar(250) not null
) engine=InnoDB;

create table tuser_role (
	user_id int(11) not null,
	role varchar(100) not null,
	primary key (user_id, role),
	foreign key (user_id) references tuser (id) on delete cascade
) engine=InnoDB;

create table tsource (
	id int(11) not null primary key auto_increment,
	user_id int(11) not null,
	name varchar(20) not null,
	description varchar(50) not null,
	lo_v varchar(100),
	hi_v varchar(100),
	lo_h varchar(100),
	hi_h varchar(100),
	rotor int(11),
	unique (user_id, name),
	foreign key (user_id) references tuser (id) on delete cascade
) engine=InnoDB;

create table ttransponder (
	id int(11) not null primary key auto_increment,
	user_id int(11) not null,
	source_id int(11) not null,
	dvbs_gen int(11) not null,
	frequency int(11) not null,
	polarity varchar(1) not null,
	symbol_rate int(11) not null,
	ignored boolean not null,
	unique (user_id, source_id, frequency, polarity),
	foreign key (user_id) references tuser (id) on delete cascade,
	foreign key (source_id) references tsource (id) on delete cascade
	) engine=InnoDB;

create table ttransp_seqno (
	transp_id int(11) not null primary key,
	user_id int(11) not null,
	seqno int(11) not null,
	unique (user_id, seqno),
	foreign key (transp_id) references ttransponder (id) on delete cascade
	) engine=InnoDB;
