package pl.robinxon.planowanie

import java.io.Serializable

class Player: Serializable {
    var points: Int = 0
    var planned = IntArray(20) {-1}
    var taken = IntArray(20) {-1}
}