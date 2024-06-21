package com.example.authentication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.authentication.R

class CardsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Проверяем, является ли активность, в которой находится фрагмент, последней
        if (requireActivity().isTaskRoot) {
            // Если активность последняя, завершаем приложение
            requireActivity().finish()
        }

        // Возвращаем разметку фрагмента (в данном случае, пустую)
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }
}
