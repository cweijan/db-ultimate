package github.cweijan.ultimate.core.component

import github.cweijan.ultimate.core.component.info.ComponentInfo
import github.cweijan.ultimate.exception.ComponentNotExistsException

object TableInfo {

    private val TypeMap: MutableMap<String?, ComponentInfo> by lazy {
        return@lazy HashMap<String?, ComponentInfo>()
    }

    val componentList: MutableList<ComponentInfo> by lazy {
        return@lazy ArrayList<ComponentInfo>()
    }

    @JvmStatic
    fun putComponent(clazz: Class<*>, componentInfo: ComponentInfo) {
        TypeMap[clazz.name] = componentInfo
        componentList.add(componentInfo)
    }

    @JvmStatic
    fun removeComponent(clazz: Class<*>?) {
        clazz ?: return
        componentList.remove(TypeMap[clazz.name])
        TypeMap.remove(clazz.name)
    }

    fun isAlreadyInit(clazz: Class<*>): Boolean {
        return TypeMap.containsKey(clazz.name)
    }


    fun getComponent(clazz: Class<*>?, nullable: Boolean = false): ComponentInfo? {

        return TypeMap[clazz?.name] ?: if (nullable) return null else throw ComponentNotExistsException("$clazz component is not exists!")
    }

    @JvmStatic
    fun getComponent(clazz: Class<*>?): ComponentInfo {

        return getComponent(clazz,false)!!
    }

    fun getTableName(clazz: Class<*>): String? {
        return getComponent(clazz).tableName;
    }

}
