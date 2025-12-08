package com.petvet.embedding.api.constants;

/**
 * PetVetEmbedding API 常量类
 */
public class ApiConstants {
	
	/**
	 * API 基础路径
	 */
	public static final String API_PREFIX = "/api/embedding";
	
	/**
	 * 简历解析API基础路径
	 */
	public static final String RESUME_API_PREFIX = "/api/resume";
	
	/**
	 * 服务名称
	 */
	public static final String SERVICE_NAME = "pet-vet-embedding";
	
	/**
	 * 简历解析 - 上传并解析
	 */
	public static final String RESUME_PARSE = RESUME_API_PREFIX + "/parse";
	
	/**
	 * 简历解析 - 查询简历信息
	 */
	public static final String RESUME_GET = RESUME_API_PREFIX + "/{resumeId}";
	
	/**
	 * 简历解析 - 语义搜索
	 */
	public static final String RESUME_SEARCH = RESUME_API_PREFIX + "/search";
	
	/**
	 * 简历解析 - 删除简历
	 */
	public static final String RESUME_DELETE = RESUME_API_PREFIX + "/{resumeId}";
	
	private ApiConstants() {
		// 工具类，禁止实例化
	}
}
