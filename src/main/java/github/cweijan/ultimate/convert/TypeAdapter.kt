package github.cweijan.ultimate.convert

import github.cweijan.ultimate.component.TableInfo
import github.cweijan.ultimate.util.DateUtils
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

object TypeAdapter {


    private val NUMBER_TYPE = Arrays.asList("byte","short", "int", "float", "double", "long", JavaType.Byte,
            JavaType.Integer, JavaType.Short, JavaType.Float, JavaType.Double, JavaType.Long)
    private val CHARACTER_TYPE = Arrays.asList(JavaType.String, "chat", JavaType.Character)
    private val BOOLEAN_TYPE = Arrays.asList(JavaType.Boolean, "boolean")
    private val DATE_TYPE = Arrays.asList("java.time.LocalTime", "java.time.LocalDateTime", "java.time.LocalDate", "java.util.Date")

    fun isAdapterType(typeName: String): Boolean {
        return NUMBER_TYPE.contains(typeName) || CHARACTER_TYPE.contains(typeName) || DATE_TYPE.contains(typeName) || BOOLEAN_TYPE.contains(typeName)
    }

    fun getAllField(componentClass: Class<*>?): List<Field> {

        val arrayList = ArrayList<Field>()
        if (componentClass == null) return arrayList
        arrayList.addAll(componentClass.declaredFields)
        arrayList.addAll(getAllField(componentClass.superclass))

        return arrayList
    }

    fun getDefaultValue(fieldType: String): Any? {
        return when {
            NUMBER_TYPE.contains(fieldType) -> 0
            BOOLEAN_TYPE.contains(fieldType) -> 0
            CHARACTER_TYPE.contains(fieldType) -> "''"
            else -> null
        }
    }

    fun convertToJavaDateObject(componentClass: Class<*>, fieldName: String, timeObject: Any): Any? {
        val columnInfo = TableInfo.getComponent(componentClass).getColumnInfoByFieldName(fieldName)!!
        val dateFormat = columnInfo.dateFormat
        val dateType = columnInfo.fieldType
        val resultTime = DateUtils.getDateFormat(dateFormat).parse(timeObject.toString())
        return when (dateType) {
            Date::class.java -> resultTime
            LocalDateTime::class.java -> LocalDateTime.ofInstant(resultTime.toInstant(), ZoneId.systemDefault())
            LocalDate::class.java -> LocalDateTime.ofInstant(resultTime.toInstant(), ZoneId.systemDefault()).toLocalDate()
            LocalTime::class.java -> LocalDateTime.ofInstant(resultTime.toInstant(), ZoneId.systemDefault()).toLocalTime()
            else -> null
        }
    }

    /**
     * convertToDateString
     */
    fun convertToDateString(componentClass: Class<*>, fieldName: String, fieldValue: Any): String {
        val dateFormat: String = TableInfo.getComponent(componentClass).getColumnInfoByFieldName(fieldName)?.dateFormat
                ?: DateUtils.DEFAULT_PATTERN
        return when (fieldValue::class.java.name) {
            Date::class.java.name -> DateUtils.getDateFormat(dateFormat).format((fieldValue as Date))
            LocalDateTime::class.java.name -> (fieldValue as LocalDateTime).format(DateUtils.getDateTimeFormatter(dateFormat))
            LocalDate::class.java.name -> (fieldValue as LocalDate).format(DateUtils.getDateTimeFormatter(dateFormat))
            LocalTime::class.java.name -> (fieldValue as LocalTime).format(DateUtils.getDateTimeFormatter(dateFormat))
            else -> "$fieldValue"
        }
    }

    /**
     * 对值进行sql兼容处理,用于insert和update语句
     */
    fun convertToSqlValue(componentClass: Class<*>, fieldName: String, fieldValue: Any): String {
        val dateFormat: String = TableInfo.getComponent(componentClass).getColumnInfoByFieldName(fieldName)?.dateFormat
                ?: DateUtils.DEFAULT_PATTERN
        val fieldType = fieldValue::class.java.name
        return when {
            NUMBER_TYPE.contains(fieldType) -> "$fieldValue"
            CHARACTER_TYPE.contains(fieldType) -> "'$fieldValue'"
            BOOLEAN_TYPE.contains(fieldType) -> if (fieldValue as Boolean) "1" else "0"
            Date::class.java.name == fieldType -> "'${DateUtils.getDateFormat(dateFormat).format((fieldValue as Date))}'"
            LocalDateTime::class.java.name == fieldType -> "'${(fieldValue as LocalDateTime).format(DateUtils.getDateTimeFormatter(dateFormat))}'"
            LocalDate::class.java.name == fieldType -> "'${(fieldValue as LocalDate).format(DateUtils.getDateTimeFormatter(dateFormat))}'"
            LocalTime::class.java.name == fieldType -> "'${(fieldValue as LocalTime).format(DateUtils.getDateTimeFormatter(dateFormat))}'"
            else -> "'$fieldValue'"
        }
    }

    fun isDateType(fieldType: String?): Boolean {
        return DATE_TYPE.contains(fieldType)
    }

}
