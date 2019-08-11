package github.cweijan.ultimate.core.dialect

import github.cweijan.ultimate.annotation.Blob
import github.cweijan.ultimate.annotation.CreateDate
import github.cweijan.ultimate.annotation.UpdateDate
import github.cweijan.ultimate.convert.TypeAdapter
import github.cweijan.ultimate.core.Query
import github.cweijan.ultimate.core.component.TableInfo
import github.cweijan.ultimate.exception.PrimaryKeyNotExistsException
import github.cweijan.ultimate.exception.PrimaryValueNotSetException
import github.cweijan.ultimate.util.DateUtils
import github.cweijan.ultimate.util.Json
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseSqlDialect : SqlDialect {

    override fun generateInsertSql(component: Any): SqlObject {

        val componentInfo = TableInfo.getComponent(component.javaClass)
        var columns = ""
        var values = ""
        val params = ArrayList<Any>()
        for (field in TypeAdapter.getAllField(componentInfo.componentClass)) {
            field.isAccessible = true
            if (componentInfo.isExcludeField(field)) continue
            val fieldValue = field.get(component)
            fieldValue?.run {
                columns += "${componentInfo.getColumnNameByFieldName(field.name)},"
                values += "?,"
                field.getAnnotation(Blob::class.java)?.let { params.add(Json.toJson(this).toByteArray()); return@run }
                params.add(TypeAdapter.convertAdapter(componentInfo.componentClass, field.name, this))
            }
            if (fieldValue == null) {
                field.getAnnotation(CreateDate::class.java)?.run {
                    columns += "${componentInfo.getColumnNameByFieldName(field.name)},"
                    values += "?,"
                    params.add(TypeAdapter.convertAdapter(componentInfo.componentClass, field.name, DateUtils.toDateString(Date(), this.value)))
                }
            }
        }
        if (columns.lastIndexOf(",") != -1) {
            columns = columns.substring(0, columns.lastIndexOf(","))
        }
        if (values.lastIndexOf(",") != -1) {
            values = values.substring(0, values.lastIndexOf(","))
        }

        return SqlObject("INSERT INTO " + componentInfo.tableName + "(" + columns + ") VALUES(" + values + ");", params)
    }

    @Throws(IllegalAccessException::class)
    override fun generateUpdateSqlByObject(component: Any, byColumn: String?): SqlObject {

        val componentInfo = TableInfo.getComponent(component.javaClass)
        val fieldName = byColumn ?: componentInfo.primaryField?.name
        ?: throw PrimaryKeyNotExistsException("invoke update must annotation table primary key!")
        val conditionFieldValue = componentInfo.getValueByFieldName(component, fieldName)
                ?: throw PrimaryValueNotSetException("update relationClass must set!")
        var sql = "UPDATE ${componentInfo.tableName} a set "
        val params = ArrayList<Any>();

        for (field in TypeAdapter.getAllField(component.javaClass)) {
            field.isAccessible = true
            if (componentInfo.isExcludeField(field) || field.name.equals(fieldName)) {
                continue
            }
            val fieldValue = field.get(component)
            fieldValue?.run {
                sql += "${componentInfo.getColumnNameByFieldName(field.name)}=?,"
                field.getAnnotation(Blob::class.java)?.let { params.add(Json.toJson(this).toByteArray()); return@run }
                params.add(TypeAdapter.convertAdapter(componentInfo.componentClass, field.name, this))
            }
            if (fieldValue == null) {
                field.getAnnotation(UpdateDate::class.java)?.run {
                    sql += "${componentInfo.getColumnNameByFieldName(field.name)}=?,"
                    params.add(TypeAdapter.convertAdapter(componentInfo.componentClass, field.name, DateUtils.toDateString(Date(), this.value)))
                }
            }
        }

        if (sql.lastIndexOf(",") == -1) {
            throw RuntimeException("Cannot find any update relationClass!")
        } else {
            sql = sql.substring(0, sql.lastIndexOf(","))
        }
        sql += " WHERE ${componentInfo.getColumnNameByFieldName(fieldName)}=?"
        params.add(conditionFieldValue)

        return SqlObject(sql, params)
    }

    override fun <T> generateDeleteSql(query: Query<T>): String {
        return "DELETE FROM ${query.component.tableName} ${generateOperationSql(query)}"
    }

    override fun <T> generateCountSql(query: Query<T>): String {
        return "select COUNT(*) count from ${query.component.tableName} ${generateOperationSql(query, true)}"
    }

    override fun <T> generateUpdateSqlByQuery(query: Query<T>): String {
        var sql = "UPDATE ${query.component.tableName} a set "

        if (!query.updateLazy.isInitialized()) throw RuntimeException("Not update column!")
        query.updateMap.keys.forEachIndexed { index, key ->
            sql += if (index == 0) "$key=?"
            else ",$key=?"
            query.addParam(query.updateMap[key])
        }
        return "$sql ${generateOperationSql(query)}"
    }

    override fun <T> generateSelectSql(query: Query<T>): String {

        val componentInfo = query.component

        val column = query.generateColumns() ?: componentInfo.selectColumns

        val sql = "select $column from ${componentInfo.tableName + generateOperationSql(query, true)}"
        return generatePaginationSql(sql, query)
    }

    private fun <T> generateOperationSql(query: Query<T>, useAlias: Boolean = false): String {

        val and = "AND"
        val or = "OR"
        var joinSql = ""
        var sql = ""

        if (query.component.joinLazy.isInitialized()) joinSql += generateJoinTablesSql(query.joinTables)

        if (query.eqLazy.isInitialized()) sql += generateOperationSql0(query.equalsOperation, "=", and, query)
        if (query.orEqLazy.isInitialized()) sql += generateOperationSql0(query.orEqualsOperation, "=", or, query)

        if (query.notEqLazy.isInitialized()) sql += generateOperationSql0(query.notEqualsOperation, "!=", and, query)
        if (query.orNotEqLazy.isInitialized()) sql += generateOperationSql0(query.orNotEqualsOperation, "!=", or, query)

        if (query.greatEqLazy.isInitialized()) sql += generateOperationSql0(query.greatEqualsOperation, ">=", and, query)
        if (query.lessEqLazy.isInitialized()) sql += generateOperationSql0(query.lessEqualsOperation, "<=", and, query)

        if (query.searchLazy.isInitialized()) sql += generateOperationSql0(query.searchOperation, "LIKE", and, query)

        if (query.isNullLazy.isInitialized()) query.isNullList.forEach { sql += "$and $it IS NULL " }
        if (query.isNotNullLazy.isInitialized()) query.isNotNullList.forEach { sql += "$and $it IS NOT NULL " }

        //生成in查询语句
        var inSql = StringBuilder()
        if (query.inLazy.isInitialized()) query.inOperation.forEach { (key, operations) ->
            inSql.append("$and $key in (")
            operations.forEach { value ->
                inSql.append(",?")
                query.addParam(value)
            }
            inSql = StringBuilder(inSql.replaceFirst(",".toRegex(), ""))
            inSql.append(")")
        }
        sql += inSql.toString()

        if (sql.startsWith(and)) {
            sql = sql.replaceFirst(and.toRegex(), "")
            sql = " WHERE$sql"
        }
        if (sql.startsWith(or)) {
            sql = sql.replaceFirst(or.toRegex(), "")
            sql = " WHERE$sql"
        }

        sql = joinSql + sql

        if (query.groupLazy.isInitialized()) query.groupByList.forEachIndexed { index, groupBy ->
            sql += if (index == 0) " GROUP BY $groupBy" else ",$groupBy"
        }
        if (query.havingLazy.isInitialized()) query.havingSqlList.forEachIndexed { index, havingSql ->
            if (index == 0) sql += " HAVING "
            sql += havingSql
        }

        if (query.orderByLazy.isInitialized()) query.orderByList.forEachIndexed { index, orderBy ->
            sql += if (index == 0) " ORDER BY $orderBy" else ",$orderBy"
        }

        return if (useAlias) query.alias + sql else sql
    }

    private fun generateJoinTablesSql(joinTables: MutableList<String>?): String {

        val sql = StringBuilder()

        joinTables?.forEach { joinTable ->
            sql.append(joinTable)
        }

        return sql.toString()
    }

    private fun <T> generateOperationSql0(operationMap: Map<String, List<Any>>?, condition: String, separator: String, query: Query<T>): String {

        val sql = StringBuilder()

        operationMap?.forEach { key, operations ->
            operations.forEach { value ->
                sql.append("$separator $key $condition ? ")
                query.addParam(value)
            }
        }

        return sql.toString()
    }

    abstract fun <T> generatePaginationSql(sql: String, query: Query<T>): String

}
