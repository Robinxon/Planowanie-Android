package pl.robinxon.planowanie

import java.io.Serializable

class Player: Serializable {
    var points: Int = 0
    var planned = arrayOfNulls<Int>(17)
    var taken = arrayOfNulls<Int>(17)
}