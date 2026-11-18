import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Kampus Kayip Esya
 * Esya kaydi, eslestirme, teslim.
 *
 * Calistirma:  javac Uygulama.java  ve  java Uygulama
 * Ana menude 0 (veya q) cikis. Ilk soruya 0 girilirse program hicbir sey yapmadan biter.
 */

// Bulunan bir esyayi temsil eder.
class Esya {
    int id;
    String ad;
    String kategori;
    String konum;
    String ayirtEdici;   // sahibinin bilmesi gereken ayirt edici ozellik (gizli)
    int beklemeGunu;     // kac gundur bekliyor
    boolean teslimEdildi;

    Esya(int id, String ad, String kategori, String konum, String ayirtEdici, int beklemeGunu) {
        this.id = id;
        this.ad = ad;
        this.kategori = kategori;
        this.konum = konum;
        this.ayirtEdici = ayirtEdici;
        this.beklemeGunu = beklemeGunu;
    }
}

public class Uygulama {

    // --- Sabitler: UC IHLAL KURALI ---
    static final int IHLAL_LIMITI = 3;
    static final long ENGEL_SURESI_MS = 60_000; // 60 saniye gecici engel

    static final String[] KATEGORILER = {"Elektronik", "Giyim", "Kimlik/Kart", "Kitap/Defter", "Diger"};

    static List<Esya> esyalar = new ArrayList<>();
    static int sonId = 0;

    // Kullanici basina ihlal sayaci ve engel bitis zamani
    static HashMap<String, Integer> ihlaller = new HashMap<>();
    static HashMap<String, Long> engelBitis = new HashMap<>();

    // ------------------------------------------------------------------
    // 1) DIZI ALIP TEK DEGER DONDUREN METOTLAR
    // ------------------------------------------------------------------

    /** Bekleme gunlerinin toplami. */
    static int toplam(int[] dizi) {
        int t = 0;
        for (int d : dizi) {
            t += d;
        }
        return t;
    }

    /** En uzun bekleme suresi (dizi bossa 0). */
    static int enBuyuk(int[] dizi) {
        if (dizi.length == 0) {
            return 0;
        }
        int en = dizi[0];
        for (int d : dizi) {
            if (d > en) {
                en = d;
            }
        }
        return en;
    }

    /** Ortalama bekleme suresi (dizi bossa 0). */
    static double ortalama(int[] dizi) {
        if (dizi.length == 0) {
            return 0;
        }
        return (double) toplam(dizi) / dizi.length;
    }

    // ------------------------------------------------------------------
    // 2) GIRDI DOGRULAYAN METOTLAR: gecerli deger girilene kadar tekrar sorar
    // ------------------------------------------------------------------

    /** min-max araliginda tam sayi girilene kadar yeniden sorar. */
    static int sayiOku(Scanner sc, String soru, int min, int max) {
        while (true) {
            System.out.print(soru + " (" + min + "-" + max + "): ");
            String satir = sc.nextLine().trim(); // girdi biterse NoSuchElementException
            try {
                int deger = Integer.parseInt(satir);
                if (deger >= min && deger <= max) {
                    return deger;
                }
                System.out.println("Gecersiz giris: deger " + min + " ile " + max + " arasinda olmali.");
            } catch (NumberFormatException e) {
                System.out.println("Gecersiz giris: lutfen bir sayi girin.");
            }
        }
    }

    /** Bos olmayan metin girilene kadar yeniden sorar. */
    static String metinOku(Scanner sc, String soru) {
        while (true) {
            System.out.print(soru + ": ");
            String satir = sc.nextLine().trim();
            if (!satir.isEmpty()) {
                return satir;
            }
            System.out.println("Gecersiz giris: bos birakilamaz.");
        }
    }

    // ------------------------------------------------------------------
    // 3) UC IHLAL KURALI
    // ------------------------------------------------------------------

    /** Kullanici su an engelli mi? Engel bittiyse temizler. */
    static boolean engelliMi(String kullanici) {
        Long bitis = engelBitis.get(kullanici);
        if (bitis == null) {
            return false;
        }
        long kalan = bitis - System.currentTimeMillis();
        if (kalan <= 0) {
            engelBitis.remove(kullanici);
            ihlaller.put(kullanici, 0);
            return false;
        }
        System.out.println("'" + kullanici + "' gecici olarak engelli. Kalan sure: "
                + (kalan / 1000 + 1) + " saniye.");
        return true;
    }

    /** Hatali talep (yanlis ayirt edici ozellik) kaydeder; 3. tekrarda engeller. */
    static void ihlalKaydet(String kullanici) {
        int sayi = ihlaller.getOrDefault(kullanici, 0) + 1;
        ihlaller.put(kullanici, sayi);
        if (sayi >= IHLAL_LIMITI) {
            engelBitis.put(kullanici, System.currentTimeMillis() + ENGEL_SURESI_MS);
            System.out.println("ENGEL: '" + kullanici + "' ayirt edici ozelligi " + IHLAL_LIMITI
                    + " kez yanlis girdigi (kural disi talep) icin "
                    + (ENGEL_SURESI_MS / 1000) + " saniye engellendi.");
        } else {
            System.out.println("Ayirt edici ozellik uyusmadi. Hatali deneme: " + sayi + "/" + IHLAL_LIMITI);
        }
    }

    // ------------------------------------------------------------------
    // Menu islemleri
    // ------------------------------------------------------------------

    static void esyaKaydet(Scanner sc) {
        System.out.println("\n--- Bulunan Esya Kaydi ---");
        String ad = metinOku(sc, "Esya adi");
        for (int i = 0; i < KATEGORILER.length; i++) {
            System.out.println("  " + (i + 1) + ") " + KATEGORILER[i]);
        }
        int kat = sayiOku(sc, "Kategori", 1, KATEGORILER.length);
        String konum = metinOku(sc, "Bulundugu yer");
        String ayirt = metinOku(sc, "Ayirt edici ozellik (sahibi dogrulamak icin, gizli kalir)");
        int gun = sayiOku(sc, "Kac gun once bulundu", 0, 365);

        Esya e = new Esya(++sonId, ad, KATEGORILER[kat - 1], konum, ayirt, gun);
        esyalar.add(e);
        System.out.println("Kaydedildi. Esya no: " + e.id);
    }

    static void eslestir(Scanner sc) {
        System.out.println("\n--- Kayip Esya Eslestirme ---");
        String aranan = metinOku(sc, "Kayip esyanin adindan bir kelime").toLowerCase();
        for (int i = 0; i < KATEGORILER.length; i++) {
            System.out.println("  " + (i + 1) + ") " + KATEGORILER[i]);
        }
        int kat = sayiOku(sc, "Kategori", 1, KATEGORILER.length);
        String kategori = KATEGORILER[kat - 1];

        int bulunan = 0;
        for (Esya e : esyalar) {
            if (e.teslimEdildi) {
                continue;
            }
            boolean adUyar = e.ad.toLowerCase().contains(aranan);
            boolean katUyar = e.kategori.equals(kategori);
            if (adUyar && katUyar) {
                System.out.println("[TAM ESLESME] " + bilgi(e));
                bulunan++;
            } else if (adUyar || katUyar) {
                System.out.println("[KISMI]       " + bilgi(e));
                bulunan++;
            }
        }
        if (bulunan == 0) {
            System.out.println("Eslesen esya bulunamadi.");
        } else {
            System.out.println("Sahibiyseniz menuden 'Esya teslim al' ile devam edin.");
        }
    }

    static String bilgi(Esya e) {
        return "#" + e.id + " " + e.ad + " | " + e.kategori + " | " + e.konum
                + " | " + e.beklemeGunu + " gun";
    }

    static void teslimEt(Scanner sc) {
        System.out.println("\n--- Esya Teslim ---");
        String kullanici = metinOku(sc, "Adiniz / ogrenci no");
        if (engelliMi(kullanici)) {
            return;
        }
        if (sonId == 0) {
            System.out.println("Kayitli esya yok.");
            return;
        }
        int id = sayiOku(sc, "Esya no", 1, sonId);
        Esya hedef = null;
        for (Esya e : esyalar) {
            if (e.id == id) {
                hedef = e;
            }
        }
        if (hedef == null || hedef.teslimEdildi) {
            System.out.println("Bu numarali teslim edilebilir esya yok.");
            return;
        }

        // AYIRT EDICI KURAL: esya yalnizca gizli ayirt edici ozelligi dogru bilene verilir.
        String iddia = metinOku(sc, "Esyanin ayirt edici ozelligi nedir");
        if (iddia.equalsIgnoreCase(hedef.ayirtEdici)) {
            hedef.teslimEdildi = true;
            ihlaller.put(kullanici, 0);
            System.out.println("Dogrulandi. '" + hedef.ad + "' " + kullanici + " adina teslim edildi.");
        } else {
            ihlalKaydet(kullanici);
        }
    }

    static void istatistik() {
        System.out.println("\n--- Bekleyen Esya Istatistigi ---");
        int[] gunler = bekleyenGunler();
        if (gunler.length == 0) {
            System.out.println("Bekleyen esya yok.");
            return;
        }
        System.out.println("Bekleyen esya sayisi : " + gunler.length);
        System.out.println("Toplam bekleme       : " + toplam(gunler) + " gun");
        System.out.println("En uzun bekleme      : " + enBuyuk(gunler) + " gun");
        System.out.printf("Ortalama bekleme     : %.1f gun%n", ortalama(gunler));
    }

    static int[] bekleyenGunler() {
        int n = 0;
        for (Esya e : esyalar) {
            if (!e.teslimEdildi) {
                n++;
            }
        }
        int[] dizi = new int[n];
        int i = 0;
        for (Esya e : esyalar) {
            if (!e.teslimEdildi) {
                dizi[i++] = e.beklemeGunu;
            }
        }
        return dizi;
    }

    static void listele() {
        System.out.println("\n--- Bekleyen Esyalar ---");
        boolean var = false;
        for (Esya e : esyalar) {
            if (!e.teslimEdildi) {
                System.out.println(bilgi(e));
                var = true;
            }
        }
        if (!var) {
            System.out.println("Bekleyen esya yok.");
        }
    }

    static void menuYaz() {
        System.out.println("\n=== KAMPUS KAYIP ESYA ===");
        System.out.println("1) Bulunan esya kaydet");
        System.out.println("2) Kayip esya ara / eslestir");
        System.out.println("3) Esya teslim al");
        System.out.println("4) Bekleyenleri listele");
        System.out.println("5) Istatistik");
        System.out.println("0) Cikis (q de olur)");
        System.out.print("Seciminiz: ");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Ornek kayitlar
        esyalar.add(new Esya(++sonId, "Siyah cuzdan", "Kimlik/Kart", "Kutuphane", "icinde mavi bilet var", 5));
        esyalar.add(new Esya(++sonId, "Kulaklik", "Elektronik", "B blok kantin", "sol kulak cizik", 12));
        esyalar.add(new Esya(++sonId, "Gri mont", "Giyim", "Amfi 3", "sag cepte anahtarlik", 2));

        try {
            // GOZCU DEGERLI DONGU: "0" veya "q" girilene kadar doner
            String secim = "";
            while (true) {
                menuYaz();
                secim = sc.nextLine().trim();
                if (secim.equals("0") || secim.equalsIgnoreCase("q")) {
                    break;
                }
                switch (secim) {
                    case "1": esyaKaydet(sc); break;
                    case "2": eslestir(sc); break;
                    case "3": teslimEt(sc); break;
                    case "4": listele(); break;
                    case "5": istatistik(); break;
                    default:
                        System.out.println("Gecersiz secim, lutfen menudeki bir deger girin.");
                }
            }
        } catch (NoSuchElementException e) {
            // Girdi beklenmedik sekilde bittiyse (ornegin pipe) duzgun cik
        }
        System.out.println("\nCikis yapildi. Gule gule!");
        sc.close();
    }
}
