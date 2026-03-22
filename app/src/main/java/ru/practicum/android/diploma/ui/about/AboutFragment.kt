package ru.practicum.android.diploma.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        val teamInfo = """
            Команда разработчиков:
            
            1. Иван Иванов - Разработчик
            2. Петр Петров - Разработчик
            3. Сергей Сергеев - Разработчик
            4. Алексей Алексеев - Разработчик
            
            Приложение для поиска работы
            Версия 1.0
        """.trimIndent()

        binding.textViewTeamInfo.text = teamInfo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
