package com.eqpos.eqentry.views.inventur;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eqpos.eqentry.databinding.ActivitySayimlarimiGosterBinding;
import com.eqpos.eqentry.databinding.ItemSayimListesiDuzenleBinding;
import com.eqpos.eqentry.db.InventurDao;
import com.eqpos.eqentry.db.SendDao;
import com.eqpos.eqentry.models.SayimModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SayimlarimiGoster extends AppCompatActivity implements SayimAdapter.OnItemClickListener {

    private ActivitySayimlarimiGosterBinding binding;
    private SayimAdapter adapter;
    private List<SayimModel> sayimList;
    private ExecutorService executor;
    private int depoId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen ayarları
        setupFullscreen();

        EdgeToEdge.enable(this);

        binding = ActivitySayimlarimiGosterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Sayımlarım");

        binding.progressBarSayimListesi.setVisibility(VISIBLE);

        initViews();
        loadData();
    }

    private void initViews() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            depoId = bundle.getInt("selectedDepoId");
        }


        initializeRecyclerView();

        binding.btnAnaMenu.setOnClickListener(view -> finish());


        binding.btnHepsiniSil.setOnClickListener(view -> {
            binding.progressBarSayimListesi.setVisibility(VISIBLE);

            executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    boolean result = InventurDao.sayimTabloyuTamamenSil();

                    runOnUiThread(() -> {
                        binding.progressBarSayimListesi.setVisibility(GONE);
                        if (result) {
                            Toast.makeText(this, "Silme işlemi başarılı", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(this, "Silme işlemi başarısız", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception ex) {
                    Log.e("HATA", "btnHepsiniGonder :: " + ex.getMessage());
                    runOnUiThread(() -> {
                        binding.progressBarSayimListesi.setVisibility(GONE);
                        Toast.makeText(this, "Hata: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });


        binding.btnHepsiniGonder.setOnClickListener(view -> {
            Log.e("HATA", "TIKLANDI: btnHepsiniGonder");
            binding.progressBarSayimListesi.setVisibility(VISIBLE);

            executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    boolean result = SendDao.sendInventur2_SayimdaKullaniyim();

                    runOnUiThread(() -> {
                        binding.progressBarSayimListesi.setVisibility(GONE);
                        if (result) {
                            Toast.makeText(this, "Gönderim başarılı", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(this, "Gönderim başarısız", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception ex) {
                    Log.e("HATA", "btnHepsiniGonder :: " + ex.getMessage());
                    runOnUiThread(() -> {
                        binding.progressBarSayimListesi.setVisibility(GONE);
                        Toast.makeText(this, "Hata: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

    }

    private void initializeRecyclerView() {
        sayimList = new ArrayList<>();
        adapter = new SayimAdapter(sayimList, this);

        binding.recyclerViewSayimlar.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSayimlar.setAdapter(adapter);
    }

    private void loadData() {
        // Arka planda verileri yükle
        new Thread(() -> {
            try {
                List<SayimModel> yeniListe = InventurDao.getSayimListem(depoId);

                runOnUiThread(() -> {
                    binding.progressBarSayimListesi.setVisibility(GONE);

                    if (yeniListe != null && !yeniListe.isEmpty()) {
                        sayimList.clear();
                        sayimList.addAll(yeniListe);
                        adapter.notifyDataSetChanged();
                        Log.d("SayimlarimiGoster", "Veri yüklendi: " + sayimList.size() + " kayıt");
                    } else {
                        Log.d("SayimlarimiGoster", "Veri bulunamadı veya liste boş");
                        // Boş liste durumu için mesaj göster
                        // binding.textEmptyMessage.setVisibility(VISIBLE);
                        Toast.makeText(this, "Veri bulunamadi veya liste boş !!!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            } catch (Exception e) {
                Log.e("SayimlarimiGoster", "Veri yükleme hatası: " + e.getMessage());
                runOnUiThread(() -> {
                    binding.progressBarSayimListesi.setVisibility(GONE);
                    // Hata mesajı göster
                });
            }
        }).start();
    }


    private void setupFullscreen() {
        // ActionBar'ı gizle
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // StatusBar'ı gizle (isteğe bağlı)
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // NavigationBar'ı gizle (Android 4.4+)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }


    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < sayimList.size()) {
            SayimModel silinecekSayim = sayimList.get(position);

            // Burada silme işlemini yap
            // InventurDao.deleteSayim(silinecekSayim);

//                    new Thread(() -> {
//                        int silinecekId = Integer.parseInt(silinecekSayim.getProductId());
//                        Log.e("Silinecek", "Silinecek : " + silinecekId);
//                        InventurDao.deleteSayim(silinecekId);
//                    });
//
//                    sayimList.remove(position);
//                    adapter.notifyItemRemoved(position);

            // Silme işlemi
            new AlertDialog.Builder(this)
                    .setTitle("Silme Onayı")
                    .setMessage(silinecekSayim.getUrunAdi() + "\n\n ürününü silmek istiyor musunuz?")
                    .setPositiveButton("Evet Sil", (dialog, which) -> {
                        // Database'den sil
                        int silinecekId = Integer.parseInt(silinecekSayim.getProductId());
                        InventurDao.deleteSayim(silinecekId);

                        sayimList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Kayıt silindi", Toast.LENGTH_SHORT).show();

                    })
                    .setNegativeButton("Hayır", null)
                    .show();

            Log.d("SayimlarimiGoster", "Silinen pozisyon: " + position);
        }
    }

    @Override
    public void onSendClick(int position) {
        if (position >= 0 && position < sayimList.size()) {
            SayimModel gonderilecekSayim = sayimList.get(position);
            binding.progressBarSayimListesi.setVisibility(VISIBLE);
//            new Thread(() -> {
//                try {
//                    int silinecekId = Integer.parseInt(gonderilecekSayim.getProductId());
//                    SendDao.sendInventurSayimdakini(silinecekId);
//                } catch (Exception ex) {
//                    Log.e("HATA", "btnHepsiniSil :: " + ex.getMessage());
//                } finally {
//                    binding.progressBarSayimListesi.setVisibility(GONE);
//                }
//            });

            executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    int silinecekId = Integer.parseInt(gonderilecekSayim.getProductId());
                    boolean result = SendDao.sendInventurSayimdakini(silinecekId);

                    runOnUiThread(() -> {
                        binding.progressBarSayimListesi.setVisibility(GONE);
                        if (result) {
                            Toast.makeText(this, "Gönderme işlemi başarılı", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(this, "Gönderme işlemi başarısız", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception ex) {
                    Log.e("HATA", "btnHepsiniGonder :: " + ex.getMessage());
                    runOnUiThread(() -> {
                        binding.progressBarSayimListesi.setVisibility(GONE);
                        Toast.makeText(this, "Hata: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });


            // Burada gönderme işlemini yap
            Log.d("SayimlarimiGoster", "onSendClick: " + gonderilecekSayim.getUrunAdi());
        }
    }

    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < sayimList.size()) {
            SayimModel editlenecekSayim = sayimList.get(position);
            showEditDialog(position);
        }
    }

    private void showEditDialog(int position) {
        SayimModel sayim = sayimList.get(position);

        // ViewBinding kullanarak dialog layout'u inflate et
        ItemSayimListesiDuzenleBinding editBinding = ItemSayimListesiDuzenleBinding.inflate(LayoutInflater.from(this));

        // Dialog u doldur
        editBinding.tvUrunAdi.setText(sayim.getUrunAdi());
        editBinding.tvGuncelMiktar.setText(sayim.getNewQuantity());
        editBinding.edYeniMiktar.setText(sayim.getNewQuantity());
        editBinding.edYeniMiktar.setSelection(editBinding.edYeniMiktar.getText().length());


        // Dialog oluştur
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(editBinding.getRoot())
                .create();


        // Buton click listener'ları
        editBinding.btnDuzenleVazgec.setOnClickListener(v -> dialog.dismiss());

        editBinding.btnDuzenleKaydet.setOnClickListener(v -> {
            String yeniMiktar = editBinding.edYeniMiktar.getText().toString().trim();

            if (!yeniMiktar.isEmpty()) {
                // Database'de güncelle
                boolean guncellendi = InventurDao.sayimMiktariGuncelle(
                        sayim.getProductId(),
                        sayim.getWarehouseId(),
                        yeniMiktar
                );

                if (guncellendi) {

                    // Adapter'ı güncelle
                    adapter.updateItem(position, yeniMiktar);

                    Toast.makeText(this, "Miktar güncellendi", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Güncelleme başarısız", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Lütfen miktar girin", Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();

    }


}