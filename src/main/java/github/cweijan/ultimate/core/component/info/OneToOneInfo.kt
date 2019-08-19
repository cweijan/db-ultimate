package github.cweijan.ultimate.core.component.info

import java.lang.reflect.Field
import kotlin.reflect.KClass

/**
 * @author cweijan
 * @version 2019/8/9 16:58
 */
class OneToOneInfo(var oneToOneField: Field, var relationColumn: String, var relationClass: KClass<*>) {
}