package com.example.traildex.data

import kotlin.random.Random

/** Varied, on-device verses for captures made without Ollama or internet access. */
object OfflineHaiku {
    private var previousIndex = -1
    private val verses = listOf(
        "Soft wings cross the pines\nThe trail holds its morning hush\nGreen light finds the leaves",
        "A small life takes root\nRain beads rest on patient leaves\nThe path wanders on",
        "Bright feathers stir\nWind carries the forest's song\nFootsteps fade to green",
        "Moss keeps the cool shade\nA quiet shape waits nearby\nClouds drift over hills",
        "Wild petals open\nBees draw circles through the sun\nSummer hums softly",
        "Under fern and stone\nTiny tracks cross the damp earth\nCreek water answers",
        "The old oak stands still\nOne brown leaf turns in the breeze\nThe day opens wide",
        "A beetle glimmers\nDew lights the edge of the trail\nDawn walks through the grass"
    )

    @Synchronized
    @Suppress("UNUSED_PARAMETER")
    fun generate(observation: String, habitat: String): String {
        var index = Random.nextInt(verses.size)
        if (verses.size > 1 && index == previousIndex) index = (index + 1) % verses.size
        previousIndex = index
        return verses[index]
    }
}
