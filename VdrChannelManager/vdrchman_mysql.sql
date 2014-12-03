create database vdrchman character set utf8 collate utf8_unicode_ci;
use vdrchman;
create user 'vdrchman'@'localhost' identified by 'vdrchman';
grant all privileges on vdrchman.* to 'vdrchman'@'localhost';

-- Application Users
create table tuser (
	id bigint(20) not null primary key auto_increment,
	name varchar(100) not null,
	password varchar(250) not null
) engine=InnoDB;

-- Application User Roles
-- 'AuthUser' role grants access to the application's main functionality
create table tuser_role (
	user_id bigint(20) not null,
	role varchar(100) not null,
	primary key (user_id, role),
	foreign key (user_id) references tuser (id) on delete cascade
) engine=InnoDB;

-- VDR Sources
-- 'name' field contains VDR Source 'code'
-- 'description' - a human readable description of the Source
-- 'lo_v', 'hi_v', 'lo_h', 'hi_h' fields corresponds to DiSEQC configuration:
-- lower band vertical polarization, higher band vertical polarization,
-- lower band horizontal polarization, higher band horizontal polarization
-- LNB settings respectively together with DiSEQC switch and/or positioner commands
-- 'rotor' field holds motor position number for the Source if any
create table tsource (
	id bigint(20) not null primary key auto_increment,
	user_id bigint(20) not null,
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

-- Source Transponders
-- 'dvbs_gen' values: 1 - DVB-S, 2 - DVB-S2
-- 'frequency' units are MHz
-- 'polarity' - 'H', 'V', 'L', 'R'
-- 'symbol_rate' units are ksyms/s
-- 'nid' - transponder's Network ID
-- 'tid' - transponder's ID within Network
-- 'ignored': if true then the Transponder won't be exported to VDR configuration
create table ttransponder (
	id bigint(20) not null primary key auto_increment,
	source_id bigint(20) not null,
	dvbs_gen int(11) not null,
	frequency int(11) not null,
	polarity varchar(1) not null,
	symbol_rate int(11) not null,
	nid int(11),
	tid int(11),
	ignored boolean not null,
	unique (source_id, frequency, polarity),
	foreign key (source_id) references tsource (id) on delete cascade
	) engine=InnoDB;

-- Transponders' order
create table ttransp_seqno (
	transp_id bigint(20) not null primary key,
	user_id bigint(20) not null,
	seqno int(11) not null,
	unique (user_id, seqno),
	foreign key (transp_id) references ttransponder (id) on delete cascade,
	foreign key (user_id) references tuser (id) on delete cascade
	) engine=InnoDB;

-- Main Channel list
-- 'sid' - Service ID
-- 'vpid' - Video PID, NULL for radio channels
-- 'venc' - video stream encoding
-- 'apid' - Audio PID
-- 'aenc' - audio stream encoding
-- 'tpid' - Text PID
-- 'caid' - Conditional Access system ID, 2600 for BISS
-- 'rid' - so called Radio ID but it seems it's not used
-- 'scanned_name' - channel's name as it is broadcasted in the stream
-- 'provider_name' - channel provider's name
-- 'name' - user preferred Channel name
-- 'lang' - channel's audio stream language
-- 'locked' - if true then the Channel won't be exported as common  
create table tchannel (
	id bigint(20) not null primary key auto_increment,
	transp_id bigint(20) not null,
	sid int(11) not null,
	vpid int(11),
	venc int(11),
	pcr int(11),
	apid int(11) not null,
	aenc int(11),
	tpid int(11),
	caid int(11),
	rid int(11),
	scanned_name varchar(50),
	provider_name varchar(50),
	name varchar(50) not null,
	lang varchar(5),
	locked boolean not null,
	unique (transp_id, sid, apid),
	foreign key (transp_id) references ttransponder (id) on delete cascade
) engine=InnoDB;

-- Channels' order
create table tchannel_seqno (
	channel_id bigint(20) not null primary key,
	user_id bigint(20) not null,
	seqno int(11) not null,
	unique (user_id, seqno),
	foreign key (channel_id) references tchannel (id) on delete cascade,
	foreign key (user_id) references tuser (id) on delete cascade
	) engine=InnoDB;

-- Channel Groups
-- 'name' - short 'code' of the channel group
-- 'start_channel_no' - a number which VDR will use to start numbering channels
-- within the group
-- 'description' - a human readable description of the group, VDR sorts groups
-- in alphabetical order so it may be useful to start description texts with
-- numbers to achieve preferred groups' order
create table tgroup (
	id bigint(20) not null primary key auto_increment,
	user_id bigint(20) not null,
	name varchar(20) not null,
	start_channel_no int(11) not null,
	description varchar(50) not null,
	ignored boolean not null,
	unique (user_id, name),
	unique (user_id, start_channel_no),
	foreign key (user_id) references tuser (id) on delete cascade
) engine=InnoDB;

-- Channel to Group relationship
create table tchannel_group (
	id bigint(20) not null primary key auto_increment,
	channel_id bigint(20) not null,
	group_id bigint(20) not null,
	unique (channel_id, group_id),
	foreign key (channel_id) references tchannel (id) on delete cascade
) engine=InnoDB;

-- Ignored Channel list
create table tignored_channel (
	id bigint(20) not null primary key auto_increment,
	transp_id bigint(20) not null,
	sid int(11) not null,
	apid int(11) not null,
	scanned_name varchar(50),
	provider_name varchar(50),
	unique (transp_id, sid, apid),
	foreign key (transp_id) references ttransponder (id) on delete cascade
) engine=InnoDB;

-- Source scanned channels
create table tscanned_channel (
	id bigint(20) not null primary key auto_increment,
	user_id bigint(20) not null,
	source_name varchar(20) not null,
	dvbs_gen int(11) not null,
	frequency int(11) not null,
	polarity varchar(1) not null,
	symbol_rate int(11) not null,
	nid int(11),
	tid int(11),
	sid int(11) not null,
	vpid int(11),
	apid int(11) not null,
	tpid int(11),
	caid int(11),
	rid int(11),
	scanned_name varchar(50),
	provider_name varchar(50),
	foreign key (user_id) references tuser (id) on delete cascade
) engine=InnoDB;
