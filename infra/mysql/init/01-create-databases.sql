CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS delivery_db;
CREATE DATABASE IF NOT EXISTS tracking_db;
CREATE DATABASE IF NOT EXISTS admin_db;

GRANT ALL PRIVILEGES ON auth_db.* TO 'smartcourier'@'%';
GRANT ALL PRIVILEGES ON delivery_db.* TO 'smartcourier'@'%';
GRANT ALL PRIVILEGES ON tracking_db.* TO 'smartcourier'@'%';
GRANT ALL PRIVILEGES ON admin_db.* TO 'smartcourier'@'%';
FLUSH PRIVILEGES;
