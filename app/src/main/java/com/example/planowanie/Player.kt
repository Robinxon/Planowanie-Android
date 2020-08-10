package com.example.planowanie

import java.io.Serializable

class Player: Serializable {
    var points: Int = 0
    var planned = IntArray(15) {-1}
    var taken = IntArray(15) {-1}
}