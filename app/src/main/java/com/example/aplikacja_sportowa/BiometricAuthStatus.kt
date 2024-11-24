package com.example.aplikacja_sportowa

enum class BiometricAuthStatus (val id: Int){
    READY( 1),
    NOT_AVAILABLE(-2),
    TEMPORARY_NOT_AVAILABLE(-3),
    AVAILABLE_BUT_NOT_ENROLLED(-4),
}