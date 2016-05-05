This directory contains a test vivo database .sql file that could be used in the example scripts.  In 
order to use it you will need to create a test instance of the database (requires MySQL admin rights) 
as follows

Run Mysql from the command line as a user which has database creation priviledges.  When prompted
enter in the password

%  mysql -u root -p

Enter the following commands to create the database

drop database if exists vitrodb_test;
create database vitrodb_test character set utf8;
use vitrodb_test;
source vitrodb-test.sql;
GRANT ALL
ON vitrodb_test.*
TO 'vitro'@'localhost' IDENTIFIED BY 'vitro123';
FLUSH PRIVILEGES;
quit



