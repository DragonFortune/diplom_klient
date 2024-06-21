package com.example.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.authentication.fragment.AccountFragment
import com.example.authentication.fragment.CalendarFragment
import com.example.authentication.fragment.CardsFragment
import com.example.authentication.fragment.ClockFragment
import com.example.authentication.fragment.HomeFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.BottomNavigationView)
        val fab_btn : FloatingActionButton = findViewById(R.id.fab_btn)
        val bottomAppBar: BottomAppBar = findViewById(R.id.bottom_app_bar)
        val homeItem = bottomNavigationView.menu.findItem(R.id.fab)
        bottomNavigationView.background = null

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fab -> {
                    bottomNavigationView.setBackgroundResource(R.color.transparent)
                    fab_btn.visibility = View.VISIBLE
                    menuItem.isEnabled = false
                    menuItem.setIcon(null)
                    bottomAppBar.fabCradleRoundedCornerRadius = 30F.toPx(this)
                    bottomAppBar.fabCradleMargin = 10F.toPx(this)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.account -> {
                    bottomNavigationView.setBackgroundResource(R.color.colorPrimary)
                    fab_btn.visibility = View.GONE
                    homeItem.isEnabled = true
                    homeItem.setIcon(R.drawable.baseline_home_24)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AccountFragment())
                        .commit()
                    true
                }
                R.id.clock -> {
                    bottomNavigationView.setBackgroundResource(R.color.colorPrimary)
                    fab_btn.visibility = View.GONE
                    homeItem.isEnabled = true
                    homeItem.setIcon(R.drawable.baseline_home_24)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ClockFragment())
                        .commit()
                    true
                }
                R.id.calendar -> {
                    bottomNavigationView.setBackgroundResource(R.color.colorPrimary)
                    fab_btn.visibility = View.GONE
                    homeItem.isEnabled = true
                    homeItem.setIcon(R.drawable.baseline_home_24)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CalendarFragment())
                        .commit()
                    true
                }
                R.id.cards -> {
                    bottomNavigationView.setBackgroundResource(R.color.colorPrimary)
                    fab_btn.visibility = View.GONE
                    homeItem.isEnabled = true
                    homeItem.setIcon(R.drawable.baseline_home_24)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CardsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        fab_btn.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            startActivity(intent)
        }
    }
}

fun Float.toPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}





