create table page
(
id INT not null auto_increment,
site_id INT not null,
path VARCHAR(255) not null,
code INT not null,
content MEDIUMTEXT not null,
primary key (id),
foreign key (site_id) references site (id),
key (path)
)