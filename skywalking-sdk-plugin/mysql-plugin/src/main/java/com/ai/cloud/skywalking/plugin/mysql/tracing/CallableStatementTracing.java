package com.ai.cloud.skywalking.plugin.mysql.tracing;

import java.sql.SQLException;

import com.ai.cloud.skywalking.buriedpoint.RPCBuriedPointSender;
import com.ai.cloud.skywalking.plugin.mysql.JDBCBuriedPointType;
import com.ai.cloud.skywalking.model.Identification;

/**
 * 连接级追踪，用于追踪用于Connection的操作追踪
 * 
 * @author wusheng
 *
 */
public class CallableStatementTracing {
	private static RPCBuriedPointSender sender = new RPCBuriedPointSender();

	public static <R> R execute(java.sql.CallableStatement realStatement,
			String connectInfo, String method, String sql, Executable<R> exec)
			throws SQLException {
		try {
			sender.beforeSend(Identification
					.newBuilder()
					.viewPoint(connectInfo)
					.businessKey(
							"callableStatement."
									+ method
									+ (sql == null || sql.length() == 0 ? ""
											: ":" + sql)).spanType(JDBCBuriedPointType.instance()).build());
			return exec.exe(realStatement, sql);
		} catch (SQLException e) {
			sender.handleException(e);
			throw e;
		} finally {
			sender.afterSend();
		}
	}

	public interface Executable<R> {
		public R exe(java.sql.CallableStatement realConnection, String sql)
				throws SQLException;
	}
}
