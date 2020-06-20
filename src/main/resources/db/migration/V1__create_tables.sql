create table URLS
(
    URL    VARCHAR(500),
    STATUS INT
) default CHARSET = utf8mb4;

create table ARTICLES
(
    ID         BIGINT auto_increment primary key,
    TITLE      TEXT,
    CONTENT    TEXT,
    URL        VARCHAR(100),
    CREATED_AT TIMESTAMP DEFAULT NOW(),
    UPDATED_AT TIMESTAMP DEFAULT NOW()
) default CHARSET = utf8mb4;
