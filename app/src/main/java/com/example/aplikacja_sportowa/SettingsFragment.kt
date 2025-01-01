package com.example.aplikacja_sportowa

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var nightModeSwitch: Switch
    private lateinit var languageSpinner: Spinner
    private lateinit var sharedPreferences: SharedPreferences
    private val NIGHT_MODE_KEY = "night_mode"
    private val LANGUAGE_KEY = "language"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        nightModeSwitch = view.findViewById(R.id.nightModeSwitch)
        languageSpinner = view.findViewById(R.id.languageSpinner)

        val isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false)
        nightModeSwitch.isChecked = isNightMode
        setTheme(isNightMode)

        nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setTheme(isChecked)
            saveThemePreference(isChecked)
        }

        setupLanguageSpinner()
        loadLanguagePreference()

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = if (position == 0) "en" else "pl"
                changeLanguage(selectedLanguage)
                saveLanguagePreference(selectedLanguage)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        return view
    }

    private fun setTheme(isNightMode: Boolean) {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun saveThemePreference(isNightMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(NIGHT_MODE_KEY, isNightMode)
        editor.apply()
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Polish")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun loadLanguagePreference() {
        val language = sharedPreferences.getString(LANGUAGE_KEY, "en")
        languageSpinner.setSelection(if (language == "en") 0 else 1)
    }

    private fun saveLanguagePreference(language: String) {
        val editor = sharedPreferences.edit()
        editor.putString(LANGUAGE_KEY, language)
        editor.apply()
    }

    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireContext().createConfigurationContext(config)
        } else {
            requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Settings"
    }
}