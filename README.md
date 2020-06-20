# j-crawler
使用 Java 实现的一个多线程爬虫

## 数据库

```bash
# 拉取 MySQL
docker run --name sina-mysql -e MYSQL_ROOT_PASSWORD=123456 -p 3306:3306 -v `pwd`/mysql-data:/var/lib/mysql -d mysql:5.7.27
```

```sql
-- 创建数据库
create database sina;
```

```bash
# 使用 FlyWay 迁移
mvn flyway:migrate
```

## 运行

直接运行 Main.java 即可。
