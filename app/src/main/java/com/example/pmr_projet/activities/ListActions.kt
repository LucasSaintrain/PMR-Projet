package com.example.pmr_projet.activities

object ListActions {

    val commandToSceneAction: HashMap<String, Pair<String, String>> = hashMapOf(
        /*Page 2*/
        "Dans son monde" to Pair("page2", "actionShowPlanetWP2"),
        "soleil brillant" to Pair("page2", "actionShowSoleil2"),
        "prit son avion" to Pair("page2", "actionHidePlanetWP2"),
        "commença le voyage" to Pair("page2", "actionAnimateBougerAvion2"),

        /*Page 4*/
        "planète Gaïa" to Pair("page4", "actionShowMonde4"),
        "Je suis le roi" to Pair("page4", "actionShowCrown4")
//        "Le petit prince gagna alors le défi" to Pair("page4", "")
        /*Page 6*/

        /*Page 8*/

        /*Page 10*/
    )

}