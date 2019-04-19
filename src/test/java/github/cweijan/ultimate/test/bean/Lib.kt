package github.cweijan.ultimate.test.bean

import github.cweijan.ultimate.annotation.Primary
import github.cweijan.ultimate.annotation.Table

@Table(alias = "l")
class Lib {

    @Primary
    var id: Int? = null
    var message: String? = null
    var test: String? = null
    var msd:String?=null
    override fun toString(): String {
        return "Lib(id=$id, message=$message, test=$test, msd=$msd)"
    }

}

