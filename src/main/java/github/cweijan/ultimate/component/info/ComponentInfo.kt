package github.cweijan.ultimate.component.info

import github.cweijan.ultimate.annotation.*
import github.cweijan.ultimate.component.TableInfo
import github.cweijan.ultimate.convert.TypeAdapter
import github.cweijan.ultimate.exception.ColumnNotExistsException
import github.cweijan.ultimate.exception.ForeignKeyNotSetException
import github.cweijan.ultimate.exception.PrimaryValueNotSetException
import github.cweijan.ultimate.util.Log
import github.cweijan.ultimate.util.StringUtils
import java.lang.reflect.Field

class ComponentInfo(var componentClass: Class<*>) {

    var primaryKey: String? = null

    private val primaryFieldName: String?
        get() {
            return primaryField?.name
        }

    private var primaryField: Field? = null

    lateinit var selectColumns: String

    lateinit var tableName: String

    var tableAlias: String? = null

    /**
     * exclude column list
     */
    private val excludeColumnList by lazy {
        return@lazy ArrayList<String>()
    }

    /**
     * 属性名与ColumnInfo的映射
     */
    private val fieldColumnInfoMap by lazy {
        return@lazy HashMap<String, ColumnInfo>()
    }

    /**
     * 列名与属性名的映射
     */
    private val columnFieldMap by lazy {
        return@lazy HashMap<String, String>()
    }

    /**
     * 外键映射
     */
    private val foreignKeyMap by lazy {
        return@lazy HashMap<Class<*>, ForeignKeyInfo>();
    }

    /**
     * 自动关联的外键列表
     */
    val autoJoinComponentList by lazy{
        return@lazy ArrayList<Class<*>>();
    }

    /**
     * 根据属性名找列名
     *
     * @param fieldName 列名
     * @return 对应的属性名
     */
    fun getColumnNameByFieldName(fieldName: String): String {

        return fieldColumnInfoMap[fieldName]?.columnName
                ?: throw ColumnNotExistsException("column field $fieldName is not exists!")
    }

    /**
     * 判断这个组件是否有属性
     */
    fun nonExistsColumn(): Boolean {
        return fieldColumnInfoMap.keys.size == 0
    }

    /**
     * 根据列名找属性名
     *
     * @param columnName 列名
     * @return 对应的属性名
     */
    fun getFieldNameByColumnName(columnName: String): String? {

        return columnFieldMap[columnName]
    }

    @Throws(IllegalAccessException::class)
    fun getPrimaryValue(component: Any): Any? {

        return primaryField!!.get(component)

    }

    fun getForeignKey(foreignClass: Class<*>): ForeignKeyInfo {
        if (!foreignKeyMap.contains(foreignClass))
            throw ForeignKeyNotSetException("${foreignClass.name} is not a valid foreign class")

        val foreignKeyInfo = foreignKeyMap[foreignClass]

        val joinKey = foreignKeyInfo!!.joinKey
        if (joinKey == "") {
            val component = TableInfo.getComponent(foreignClass)
            if (component.primaryKey == null || component.primaryKey == "") {
                throw PrimaryValueNotSetException("join component ${foreignClass.name} is not primary key found! ")
            } else {
                foreignKeyInfo.joinKey = component.primaryKey!!
            }
        }

        return foreignKeyInfo
    }

    fun isPrimaryField(field: Field?): Boolean {
        return null != field && field.name == primaryFieldName
    }

    fun isExcludeField(field: Field?): Boolean {
        return null != field && excludeColumnList.contains(field.name)
    }

    private fun putColumn(fieldName: String, columnInfo: ColumnInfo) {

        fieldColumnInfoMap[fieldName] = columnInfo
        columnFieldMap[columnInfo.columnName] = fieldName
    }

    companion object {

        /**
         * 生成component信息
         *
         * @param componentClass 实体类
         */
        fun init(componentClass: Class<*>, scanMode: Boolean = true): ComponentInfo? {

            if (TableInfo.isAlreadyInit(componentClass) && scanMode) return null
            val table = componentClass.getAnnotation(Table::class.java) ?: return null
            var tableName = table.value
            if (tableName == "") {
                tableName = componentClass.simpleName.toLowerCase()
            }

            val componentInfo = ComponentInfo(componentClass)
            componentInfo.tableName = tableName
            componentInfo.selectColumns = table.selectColumns
            componentInfo.tableAlias = table.alias
            generateColumns(componentInfo, table.camelcaseToUnderLine)
            TableInfo.putComponent(componentClass, componentInfo)
            Log.logger.debug("load component ${componentClass.name}, table is $tableName")
            return componentInfo
        }

        /**
         * 生成component的列信息
         *
         * @param componentInfo component实例
         * @param camelcaseToUnderLine         是否将驼峰变量转为下划线列名
         */
        private fun generateColumns(componentInfo: ComponentInfo, camelcaseToUnderLine: Boolean = true) {

            val fields = componentInfo.componentClass.declaredFields
            var columnInfo: ColumnInfo

            for (field in fields) {
                field.isAccessible = true
                //是否是exclude
                if (field.getAnnotation(Exclude::class.java) != null) {
                    componentInfo.excludeColumnList.add(field.name)
                    continue
                }
                columnInfo = ColumnInfo()

                //生成column name
                val columnAnnotation = field.getAnnotation(Column::class.java)
                if (StringUtils.isNotEmpty(columnAnnotation?.value)) {
                    columnInfo.columnName = columnAnnotation.value
                    columnInfo.isNullable = columnAnnotation.nullable
                    if (columnAnnotation.length != 0) {
                        columnInfo.length = columnAnnotation.length
                    }
                } else {
                    columnInfo.columnName = field.name
                }

                if (camelcaseToUnderLine) {
                    val regex = Regex("([a-z])([A-Z]+)")
                    val replacement = "$1_$2"
                    columnInfo.columnName = columnInfo.columnName.replace(regex, replacement).toLowerCase()
                }

                //生成primary key column info
                val primaryAnnotation = field.getAnnotation(Primary::class.java)
                if (primaryAnnotation != null || (field.name == "id" && StringUtils.isEmpty(componentInfo.primaryKey))) {
                    componentInfo.primaryKey = columnInfo.columnName
                    componentInfo.primaryField = field
                    columnInfo.isAutoIncrement = primaryAnnotation?.autoIncrement ?: false
                }

                //生成foreign key column info
                val foreignKeyAnnotation = field.getAnnotation(ForeignKey::class.java)
                foreignKeyAnnotation?.run {
                    var joinColumnName = joinColumn
                    if (camelcaseToUnderLine) {
                        val regex = Regex("([a-z])([A-Z]+)")
                        val replacement = "$1_$2"
                        joinColumnName = joinColumn.replace(regex, replacement).toLowerCase()
                    }
                    if(autoJoin){
                        componentInfo.autoJoinComponentList.add(value.java)
                    }
                    val foreignKeyInfo = ForeignKeyInfo(columnInfo.columnName, joinColumnName)
                    componentInfo.foreignKeyMap.put(value.java, foreignKeyInfo)
                }

                componentInfo.putColumn(field.name, columnInfo)
            }

        }
    }

}
