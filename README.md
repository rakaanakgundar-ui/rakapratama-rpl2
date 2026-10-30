# Kaboy Garage — Desktop Swing + MySQL

Aplikasi desktop **Java Swing** untuk pengelolaan bengkel motor **Kaboy Garage**.
Aplikasi menangani katalog spare part, pemesanan spare part, data pelanggan dan
kendaraan, serta waiting list servis. Data disimpan di **MySQL/MariaDB XAMPP**
melalui **JDBC**.

Project ini adalah versi **GUI Desktop** dari tugas Praktikum RPL 2. Versi web
(Maven + Tomcat) tetap disimpan terpisah dan tidak diubah oleh project ini.

- **Nama** : Raka Pratama Putra Sodiq
- **NPM** : 51423218
- **Kelas** : 4IA24
- **Materi** : Desain Perangkat Lunak
- **Teknologi** : Java 11+, Java Swing, JDBC, MySQL/MariaDB, NetBeans (Java with Ant)

### Perbedaan dengan versi web

| Aspek | Versi web (tetap terpisah) | Versi desktop (project ini) |
|---|---|---|
| Antarmuka | Servlet/JSP di browser | Java Swing |
| Server | Apache Tomcat | Tidak perlu server web |
| ORM | Hibernate | JDBC langsung |
| Build | Maven (`pom.xml`) | Ant NetBeans (`build.xml`) |
| Entry point | Web archive | `kaboygarage.gui.GuiMain` |

---

## 1. Fitur

1. **Login admin** dari tabel `admin`; password disimpan sebagai hash SHA-256.
2. **Dashboard** berisi jumlah spare part, pesanan aktif, antrean servis aktif,
   total pelanggan, daftar stok menipis, pesanan terbaru, dan antrean terbaru.
3. **Spare Part** — CRUD katalog, pencarian kode/nama/merk, harga, stok,
   stok minimal, kategori, dan status stok (`Tersedia`, `Menipis`, `Habis`).
4. **Pelanggan & Kendaraan** — CRUD pelanggan dan kendaraan yang saling
   berelasi. Kendaraan hanya bisa diservis oleh pemiliknya.
5. **Pesanan** — membuat pesanan dari beberapa spare part, menghitung total
   otomatis, mengurangi stok secara atomik, dan mengubah status pesanan.
6. **Waiting List Servis** — pendaftaran servis, nomor antrean harian, jadwal
   servis, estimasi selesai, dan perubahan status servis.
7. **GUI konsisten** — sidebar navigasi, kartu statistik, tabel, form, dialog
   konfirmasi, dan tema Nimbus.

---

## 2. Kebutuhan

- **JDK 11 / 17 / 21** (project dikonfigurasi `javac.source=11`)
- **NetBeans 17** (project **Java with Ant**)
- **XAMPP** dengan MySQL/MariaDB berjalan di port **3306**
- Driver **mysql-connector-j-8.3.0.jar** (sudah disertakan di `lib/`)

---

## 3. Cara Menjalankan

### 3.1 Jalankan MySQL XAMPP

Buka **XAMPP Control Panel**, lalu klik **Start** pada **MySQL**.

### 3.2 Buat database

Dari folder project, jalankan:

```bat
setup_database.bat
```

Atau import manual `database/kaboy_garage.sql` melalui **phpMyAdmin**
(`http://localhost/phpmyadmin`).

Script tersebut membuat database `kaboy_garage`, seluruh tabel, akun admin,
5 spare part contoh, serta 3 pelanggan dan 3 kendaraan contoh.

### 3.3 Buka di NetBeans

1. Buka **NetBeans 17**.
2. Pilih **File > Open Project**.
3. Arahkan ke folder project ini, lalu klik **Open Project**.
4. Pastikan project dikenali sebagai **Java with Ant**.
5. Tekan **F6** atau klik kanan project > **Run**.

Main class sudah diatur ke `kaboygarage.gui.GuiMain` pada
`nbproject/project.properties`.

### 3.4 Login

| Username | Password |
|----------|----------|
| `admin`  | `admin123` |

Password disimpan sebagai hash SHA-256 di tabel `admin`.

### 3.5 Menjalankan dari command line

```bat
build.bat
```

Script tersebut mengompilasi seluruh source, lalu menjalankan GUI.

Manual:

```bat
dir /s /b src\*.java > sources.txt
javac --release 11 -encoding UTF-8 -cp lib\mysql-connector-j-8.3.0.jar -d build\classes @sources.txt
java -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.gui.GuiMain
```

Dengan Ant/NetBeans:

```bat
ant clean run
```

`ant clean jar` juga menghasilkan `dist/KaboyGarage.jar`, tetapi driver JDBC
tidak ikut dibundel. Jalankan JAR dengan classpath driver:

```bat
java -cp "dist\KaboyGarage.jar;lib\mysql-connector-j-8.3.0.jar" kaboygarage.gui.GuiMain
```

---

## 4. Konfigurasi Database

Konfigurasi ada di `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/kaboy_garage?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta&characterEncoding=UTF-8
db.user=root
db.password=
```

Jika MySQL memakai password, isi `db.password`. Nilai ini juga bisa ditimpa
lewat parameter JVM:

```bat
java -Dkaboy.db.user=root -Dkaboy.db.password=rahasia ^
     -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.gui.GuiMain
```

---

## 5. Struktur Project

```text
KaboyGarage/
├── database/
│   └── kaboy_garage.sql          # schema + akun admin + data contoh
├── docs/
│   ├── screenshots/              # tangkapan layar GUI
│   └── struktur-oop.md           # class diagram & penjelasan desain OOP
├── lib/
│   └── mysql-connector-j-8.3.0.jar
├── nbproject/                    # konfigurasi project NetBeans Ant
├── src/kaboygarage/
│   ├── dao/                      # akses database (JDBC)
│   │   ├── AdminDao.java
│   │   ├── KendaraanDao.java
│   │   ├── PelangganDao.java
│   │   ├── PendaftaranServisDao.java
│   │   ├── PesananDao.java
│   │   └── SparePartDao.java
│   ├── db/
│   │   ├── Database.java         # koneksi + transaksi
│   │   └── DataAccessException.java
│   ├── dto/
│   │   ├── ItemPesanan.java      # item keranjang sebelum disimpan
│   │   └── Statistik.java        # ringkasan dashboard
│   ├── exception/
│   │   ├── AplikasiException.java
│   │   └── StokTidakCukupException.java
│   ├── gui/
│   │   ├── GuiMain.java          # entry point GUI
│   │   ├── LoginFrame.java
│   │   ├── MainFrame.java        # sidebar + CardLayout
│   │   ├── DashboardPanel.java
│   │   ├── SparePartPanel.java
│   │   ├── PelangganPanel.java
│   │   ├── PesananPanel.java
│   │   ├── WaitingListPanel.java
│   │   ├── Theme.java
│   │   ├── FormatUtil.java
│   │   └── Refreshable.java
│   ├── main/
│   │   └── Main.java             # launcher kompatibilitas ke GuiMain
│   ├── model/                    # model OOP + aturan domain
│   │   ├── Orang.java            # abstract class
│   │   ├── Admin.java
│   │   ├── Pelanggan.java
│   │   ├── Kendaraan.java
│   │   ├── SparePart.java
│   │   ├── DetailPesanan.java
│   │   ├── Pesanan.java
│   │   ├── PendaftaranServis.java
│   │   ├── KategoriSparePart.java
│   │   ├── StatusPesanan.java
│   │   ├── StatusServis.java
│   │   └── JenisServis.java
│   ├── service/                  # logika bisnis + transaksi
│   │   ├── AuthService.java
│   │   ├── DashboardService.java
│   │   ├── PelangganService.java
│   │   ├── PesananService.java
│   │   ├── SparePartService.java
│   │   └── WaitingListService.java
│   ├── tools/
│   │   ├── SmokeTest.java        # uji DAO + alur bisnis
│   │   └── GuiSmokeTest.java     # uji inisialisasi seluruh panel
│   └── util/
│       ├── KodeGenerator.java
│       ├── PasswordUtil.java
│       └── Validasi.java
├── build.bat
├── build.xml
├── setup_database.bat
├── db.properties
├── manifest.mf
├── README.md
├── CODE_LENGKAP.md
├── demo-output.txt
├── Laporan Akhir Praktikum KaboyGarage.docx
└── Laporan Codingan KaboyGarage.docx
```

### Pembagian 3-tier

| Layer | Package | Isi |
|---|---|---|
| Presentation | `gui` | Frame, panel, form, tabel, dialog Swing |
| Application | `service`, `model`, `dto`, `util`, `exception` | Aturan bisnis, model domain, validasi, transaksi |
| Data | `dao`, `db` | JDBC, SQL, mapping ResultSet, koneksi |
| Database | MySQL/MariaDB | Tabel `admin`, `pelanggan`, `kendaraan`, `spare_part`, `pesanan`, `detail_pesanan`, `pendaftaran_servis` |

---

## 6. Tabel Database

| Tabel | Keterangan |
|-------|------------|
| `admin` | akun admin + hash password |
| `pelanggan` | data pelanggan |
| `kendaraan` | kendaraan milik pelanggan |
| `spare_part` | katalog spare part + stok |
| `pesanan` | header pesanan + status |
| `detail_pesanan` | item pesanan + harga snapshot |
| `pendaftaran_servis` | antrean servis + jadwal + status |

---

## 7. Aturan Bisnis

- Satu pelanggan dapat memiliki banyak kendaraan, pesanan, dan pendaftaran servis.
- Satu pesanan memiliki banyak detail; total dihitung dari `subtotal` setiap detail.
- Harga satuan pada detail pesanan disimpan sebagai **snapshot**, sehingga
  perubahan harga katalog tidak mengubah total pesanan lama.
- Stok spare part hanya berubah melalui operasi domain yang tervalidasi.
  Pembuatan pesanan dan pengurangan stok dijalankan dalam **satu transaksi**.
- Status pesanan hanya boleh bergerak:
  `BARU -> DIPROSES -> SIAP DIAMBIL -> SELESAI`.
  Pesanan dapat dibatalkan dari `BARU` atau `DIPROSES`, dan stok dikembalikan.
- Status servis hanya boleh bergerak:
  `MENUNGGU -> DIJADWALKAN -> DIKERJAKAN -> SELESAI`.
  Servis dapat dibatalkan dari `MENUNGGU` atau `DIJADWALKAN`.
- Jadwal servis tidak boleh sebelum tanggal pendaftaran.
- Kendaraan yang dipilih pada pendaftaran servis harus milik pelanggan tersebut.
- Spare part yang sudah dipakai pada detail pesanan tidak bisa dihapus.
- Pelanggan yang masih memiliki kendaraan/pesanan/servis tidak bisa dihapus
  (dijaga oleh foreign key database).

---

## 8. Uji Cepat

Setelah database dibuat dan source dikompilasi:

```bat
java -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.tools.SmokeTest
java -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.tools.GuiSmokeTest
```

`SmokeTest` menguji login, katalog, CRUD pelanggan/kendaraan, pembuatan pesanan,
pengurangan dan pengembalian stok, transisi status pesanan, pendaftaran servis,
penjadwalan, transisi status servis, dan statistik dashboard. Data uji
dibersihkan otomatis.

`GuiSmokeTest` membuat seluruh frame/panel Swing dan memuat data dari database
untuk memastikan tidak ada error inisialisasi.

Output nyata tersimpan di `demo-output.txt`.

---

## 9. Troubleshooting

| Gejala | Solusi |
|--------|--------|
| `Communications link failure` | MySQL XAMPP belum Start atau port bukan 3306. |
| `Unknown database 'kaboy_garage'` | Jalankan `setup_database.bat` atau import SQL manual. |
| `Access denied for user 'root'` | Isi `db.password` di `db.properties`. |
| `No suitable driver` / `ClassNotFoundException` | Pastikan `lib/mysql-connector-j-8.3.0.jar` ada dan masuk classpath. |
| Tampilan GUI tidak muncul | Jalankan dari NetBeans (F6), `ant run`, atau `build.bat`. |
| Login gagal padahal password benar | Cek tabel `admin`; seed default `admin` / `admin123`. |
| Data contoh tidak muncul | Import ulang `database/kaboy_garage.sql`; script memakai `INSERT IGNORE`, jadi data yang sudah ada tidak ditimpa. |

---

## 10. Catatan

- `CODE_LENGKAP.md` berisi kompilasi seluruh source Java dan script database.
- `demo-output.txt` adalah output nyata `SmokeTest` dan `GuiSmokeTest`.
- `docs/struktur-oop.md` menjelaskan class diagram, relasi, dan penerapan
  konsep OOP.
- `docs/screenshots/` berisi tangkapan layar GUI untuk lampiran laporan.
- `Laporan Akhir Praktikum KaboyGarage.docx` adalah laporan naratif.
- `Laporan Codingan KaboyGarage.docx` adalah daftar kode lengkap.
- Versi web Maven/Tomcat tetap berada di folder terpisah
  (`Documents/NetBeansProjects/KaboyGarage`) dan tidak diubah.
- Project ini dibuat untuk tugas Praktikum Rekayasa Perangkat Lunak 2.
