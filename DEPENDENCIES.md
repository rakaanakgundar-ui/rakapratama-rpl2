# Dependensi — KaboyGarage

## Wajib

| Komponen | Versi | Peran | Lokasi |
|----------|-------|-------|--------|
| **JDK** | 11 / 17 / 21 | Compiler & runtime | System PATH |
| **MySQL / MariaDB** | 5.7+ | Database server | XAMPP |
| **mysql-connector-j** | 8.3.0 | JDBC driver | `lib/mysql-connector-j-8.3.0.jar` |

## Build Tools

| Tool | Versi | Peran |
|------|-------|-------|
| **NetBeans** | 17+ | IDE (Ant runner) |
| **Ant** | 1.10+ | Build automation (built-in NetBeans) |

## Opsional

| Tool | Kegunaan |
|------|----------|
| **XAMPP** | Paket MySQL + phpMyAdmin |
| **Git** | Version control |

## Setup

### 1. JDK
```bash
java -version
```
Pastikan JDK 11+ terinstall dan PATH ter-set.

### 2. MySQL (XAMPP)
- Download XAMPP dari https://www.apachefriends.org
- Install, lalu mulai **MySQL** dari Control Panel
- Default: user=`root`, password=kosong, port=3306

### 3. mysql-connector-j
Sudah ada di `lib/mysql-connector-j-8.3.0.jar`. NetBeans otomatis include dalam classpath Ant.

### 4. NetBeans + Ant
- NetBeans 17+ sudah include Ant
- File konfigurasi: `nbproject/project.properties` (sudah dikonfigurasi)

## Verifikasi

```bash
# Cek JDK
java -version

# Cek MySQL berjalan
netstat -an | find ":3306"    (Windows)
netstat -an | grep ":3306"    (Linux/Mac)

# Cek JDBC driver
cd lib && ls -la mysql-connector-j-8.3.0.jar
```

## Troubleshooting

| Error | Penyebab | Solusi |
|-------|----------|--------|
| `java: command not found` | JDK tidak di PATH | Install JDK, set JAVA_HOME |
| `Communications link failure` | MySQL offline / port salah | Start MySQL XAMPP, cek port 3306 |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Driver tidak di classpath | Pastikan `lib/mysql-connector-j-8.3.0.jar` ada |
| Build gagal di NetBeans | Ant path salah | Tools → Options → Java → Ant Home, pilih NetBeans Ant |

## Override Koneksi DB

Edit `db.properties` atau pakai JVM args:
```bash
java -Dkaboy.db.user=root -Dkaboy.db.password=rahasia \
     -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" \
     kaboygarage.gui.GuiMain
```

## Catatan Kompatibilitas

- Project dikompilasi ke **Java 11** (`javac.source=11`)
- Berjalan di **Java 11 / 17 / 21** (forward-compatible)
- Swing built-in (sejak Java 1.1)
- Tidak perlu library UI tambahan (Nimbus Look & Feel adalah standard)
