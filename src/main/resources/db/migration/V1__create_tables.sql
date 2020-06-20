create table URLS
(
    URL    VARCHAR(500),
    STATUS INT
);

create table ARTICLES
(
    ID         BIGINT auto_increment primary key,
    TITLE      TEXT,
    CONTENT    TEXT,
    URL        VARCHAR(100),
    CREATED_AT TIMESTAMP,
    UPDATED_AT TIMESTAMP
);
