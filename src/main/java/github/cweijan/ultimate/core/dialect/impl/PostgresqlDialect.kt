package github.cweijan.ultimate.core.dialect.impl

import github.cweijan.ultimate.core.Query
import github.cweijan.ultimate.core.dialect.BaseSqlDialect

class PostgresqlDialect : BaseSqlDialect() {

    override fun <T> generatePaginationSql(sql: String, query: Query<T>): String {

        if (null == query.offset && null == query.pageSize) return sql

        return "$sql limit ${query.pageSize} offset ${query.offset ?: 0}"
    }

}