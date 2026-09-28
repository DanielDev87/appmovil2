package com.danidev.appmovil2

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerNameStorageTest {
    @Test
    fun saveAndLoadPlayerName() {
        val storage = InMemoryPlayerNameStorage()

        storage.saveName("Ana")

        assertEquals("Ana", storage.getName())
    }
}
