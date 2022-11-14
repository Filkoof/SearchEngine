create table page
(
id INT not null auto_increment,
site_id INT not null,
path TEXT not null,
code INT not null,
content MEDIUMTEXT not null,
primary key (id)
)