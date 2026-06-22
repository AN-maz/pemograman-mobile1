# Pemrograman Mobile 1

## Project Tugas Besar

### Deskripsi Proyek

Proyek ini merupakan tugas besar mata kuliah **Pemrograman Mobile 1** yang dikerjakan secara berkelompok oleh 4 mahasiswa. Aplikasi ini dikembangkan untuk mengimplementasikan konsep-konsep pemrograman mobile yang telah dipelajari selama perkuliahan, seperti desain antarmuka, navigasi, manajemen data, dan fitur-fitur mobile lainnya.

### Tujuan Proyek

- Menerapkan konsep dasar pengembangan aplikasi mobile.
- Melatih kerja sama tim dalam pengembangan perangkat lunak.
- Menghasilkan aplikasi yang fungsional dan mudah digunakan.
- Mengimplementasikan praktik pengembangan perangkat lunak yang baik.

### Anggota Kelompok

| No  | Nama                    |
| --- | ----------------------- |
| 1   | Adrian Fathurahman      |
| 2   | Andrian Maulana Dzikwan |
| 3   | Fahrizal                |
| 4   | Muhammad Rifaldy        |

### Teknologi yang Digunakan

- Android Studio
- Kotlin
- MySQL / Firebase
- XML Layout
- Git & GitHub

---

# Use Case Diagram

### 1. Sistem Internal
![Sistem Internal](UC/sistem-internal.png)

### 2. Sistem Operasional
![Sistem Operasional](UC/sistem-operasional.png)

### 3. Sistem Visit Track
![Sistem Visit Track](UC/sistem-visit-track.png)

### 4. Sistem Super Apps
![Sistem Super Apps](UC/sistem-super-apps.png)

---

### Standar Penamaan Branch

#### - `feature/nama-fitur` (untuk membuat fitur baru)
#### - `bugfix/nama-bug` (untuk memperbaiki error)
#### - `chore/nama-tugas` (untuk tugas teknis non-fitur, seperti pengaturan library)

### Siklus Kerja Member
Setiap kali ingin membuat fitur baru, alurnya:
#### Langkah A: Pindah ke branch `testing` (bukan `main`), lalu ambil kode terbaru

```bash
git checkout testing
git pull origin testing
```

#### Langkah B: Buat branch fitur baru dari branch `testing` tersebut.
```bash
git checkout -b fitur/login
```

#### Langkah C: Coding di Android Studio sampai fiturnya selesai. Jika sudah selesai, lakukan `commit`.
```bash
git add .
git commit -m "menyelesaikan fitur login"
```

#### Langkah D: Push branch fitur tersebut ke GitHub.
```bash
git push origin fitur/login
```
