
/**
 * 尝试从 [System.getProperty] 和 [System.getenv] 中获取指定属性。
 * 优先使用 [System.getProperty]。
 */
fun systemProp(propKey: String, envKey: String = propKey): String? =
    System.getProperty(propKey) ?: System.getenv(envKey)

