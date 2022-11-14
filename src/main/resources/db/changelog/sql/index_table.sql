create table search_index
(
id INT not null auto_increment,
page_id INT not null,
lemma_id INT not null,
lemma_rank FLOAT not null,
primary key (id)
)