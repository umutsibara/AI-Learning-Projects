package com.umutsibara.chess_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.umutsibara.chess_app.databinding.ActivityMainBinding
import android.view.View
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity(), ClockFragment.OnClockInteractionListener {

    // ViewBinding için tanımlama doğru.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Başlangıç fragment'ını yükle
        if (savedInstanceState == null) {
            loadFragment(PlayFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_play -> PlayFragment()
                R.id.navigation_clock -> ClockFragment()
                R.id.navigation_fen -> FenFragment()
                else -> PlayFragment()
            }
            loadFragment(selectedFragment)
            true
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Mevcut fragment'ı bul
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                // Eğer mevcut fragment ClockFragment ise ve geri tuşunu o yönettiyse...
                if (currentFragment is ClockFragment && currentFragment.handleBackPress()) {
                    // Hiçbir şey yapma, çünkü ClockFragment zaten her şeyi halletti.
                } else {
                    // Aksi halde, varsayılan geri tuşu davranışını uygula (örn. uygulamadan çık)
                    // Callback'i geçici olarak devre dışı bırakıp tekrar basılmasını sağlıyoruz.
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Arayüz (interface) metotları
    // Bu metotlarda da artık `binding` nesnesini kullanıyoruz.
    override fun hideBottomNav() {
        binding.bottomNavigation.visibility = View.GONE
    }

    override fun showBottomNav() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }
}