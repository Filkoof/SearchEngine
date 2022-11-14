create table lemma
(
id INT not null auto_increment,
site_id INT not null,
lemma VARCHAR(255) not null,
frequency INT not null,
primary key (id),
foreign key (site_id) references site (id)
)