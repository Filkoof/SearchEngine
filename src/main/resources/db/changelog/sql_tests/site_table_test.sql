create table site
(
id INT not null auto_increment,
status ENUM('INDEXING', 'INDEXED', 'FAILED') not null,
status_time DATETIME not null,
last_error TEXT,
url VARCHAR(255) not null,
name VARCHAR(255) not null,
primary key (id)
)