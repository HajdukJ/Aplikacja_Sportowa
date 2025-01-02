package com.example.aplikacja_sportowa

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Klasa odpowiedzialna za uwierzytelnianie biometryczne użytkownika.
 * Używa API BiometricPrompt do obsługi autoryzacji.
 *
 * @param activity Aktywność, w której działa uwierzytelnianie biometryczne.
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {

    /**
     * Wywołuje proces uwierzytelniania biometrycznego.
     *
     * @param title Tytuł wyświetlany w oknie dialogowym.
     * @param subTitle Podtytuł wyświetlany w oknie dialogowym.
     * @param negativeButtonText Tekst przycisku anulowania.
     * @param onSuccess Funkcja wywoływana, gdy uwierzytelnianie zakończy się sukcesem.
     * @param onError Funkcja wywoływana, gdy wystąpi błąd podczas uwierzytelniania.
     *                Przyjmuje kod błędu i opis błędu jako parametry.
     * @param onFailed Funkcja wywoływana, gdy uwierzytelnianie nie powiedzie się (np. niewłaściwy odcisk palca).
     */
    fun promptBiometricAuth(
        title: String,
        subTitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit,
        onFailed: () -> Unit
    ) {
        // Uzyskanie głównego wykonawcy (executor) do przetwarzania wyników uwierzytelniania.
        val executor = ContextCompat.getMainExecutor(activity)

        // Tworzenie obiektu BiometricPrompt z ustawionymi wywołaniami zwrotnymi (callback).
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            /**
             * Wywoływane, gdy uwierzytelnianie zakończy się sukcesem.
             *
             * @param result Wynik uwierzytelniania.
             */
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            /**
             * Wywoływane, gdy uwierzytelnianie nie powiedzie się.
             * Użytkownik podał niewłaściwy odcisk palca lub inną formę biometryczną.
             */
            override fun onAuthenticationFailed() {
                onFailed()
            }

            /**
             * Wywoływane, gdy wystąpi błąd podczas uwierzytelniania.
             *
             * @param errorCode Kod błędu, który opisuje problem.
             * @param errString Opis błędu w formie tekstowej.
             */
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString.toString())
            }
        })

        // Konfiguracja okna dialogowego uwierzytelniania.
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subTitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        // Rozpoczęcie uwierzytelniania.
        biometricPrompt.authenticate(promptInfo)
    }
}