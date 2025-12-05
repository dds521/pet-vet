package com.petvet.embedding.api.enums;

/**
 * 向量数据库类型枚举
 */
public enum VectorDatabaseType {
	/**
	 * Qdrant 向量数据库
	 */
	QDRANT("qdrant"),
	
	/**
	 * Zilliz 向量数据库
	 */
	ZILLIZ("zilliz");
	
	private final String value;
	
	VectorDatabaseType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * 根据字符串值获取枚举
	 *
	 * @param value 字符串值
	 * @return 对应的枚举，如果不存在则返回 QDRANT
	 */
	public static VectorDatabaseType fromValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			return QDRANT;
		}
		for (VectorDatabaseType type : values()) {
			if (type.value.equalsIgnoreCase(value.trim())) {
				return type;
			}
		}
		return QDRANT; // 默认返回 QDRANT
	}
}
