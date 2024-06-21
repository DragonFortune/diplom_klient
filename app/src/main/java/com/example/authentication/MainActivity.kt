package com.example.authentication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val texts = listOf("Text 1", "Text 2", "Text 3")

        val sharedPreferences = getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token != null) {
            startActivity(Intent(this, HomePage::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val regBtn: Button = findViewById(R.id.registration_btn)
        val logBtn: Button = findViewById(R.id.sign_in_btn)
//        val test: Button = findViewById(R.id.test)

        regBtn.setOnClickListener {
            try {
                val intent = Intent(this, Registration::class.java)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        logBtn.setOnClickListener {
            try {
                val intent = Intent(this, LogIn::class.java)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//        test.setOnClickListener {
//            try {
//                val intent = Intent(this, HomePage::class.java)
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivity(intent)
//                } else {
//                    Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

//        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
//        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
//
//        val adapter = TextPagerAdapter(texts)
//        viewPager.adapter = adapter
//
//        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//            tab.text = "Item ${position + 1}"
//        }.attach()
    }
}

class TextPagerAdapter(private val texts: List<String>) : RecyclerView.Adapter<TextPagerAdapter.TextViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.textView.text = texts[position]
    }

    override fun getItemCount() = texts.size

    class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }
}
