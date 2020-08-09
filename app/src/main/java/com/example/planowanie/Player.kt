package com.example.planowanie

import java.io.Serializable

class Player: Serializable {
    var points: Int = 0
    lateinit var planned: Array<Int>
    lateinit var taken: Array<Int>
}