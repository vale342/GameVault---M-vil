package com.example.gamehealthmanager.core
//Definir el contrato para comunicarse entre fragmentos
interface FragmentCommunicator {
    fun manageLoader (isVisible: Boolean)
}
//Quien lo implementará será el activityMain, porque ahi está el loader