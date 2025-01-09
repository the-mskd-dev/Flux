package com.kaem.flux.model

import org.junit.Test


class UserFileTest {

    @Test
    fun parsing_file_name() {

        val files = listOf(
            "Naruto_s02e09.mp4",
            "Spider-man(2001).mp4"
        )

        val naruto = FileNameProperties.fromFileName(files[0])
        val spiderman = FileNameProperties.fromFileName(files[1])

        assert(naruto.title == "naruto")
        assert(naruto.season == 2)
        assert(naruto.episode == 9)
        assert(naruto.year == null)

        assert(spiderman.title == "spider-man(2001)")
        assert(spiderman.season == null)
        assert(spiderman.episode == null)
        assert(spiderman.year == 2001)

    }

}