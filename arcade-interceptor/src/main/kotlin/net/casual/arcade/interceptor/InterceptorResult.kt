package net.casual.arcade.interceptor

public sealed interface InterceptorResult {
    public data object Pass: InterceptorResult
    public data object Cancel: InterceptorResult
    public data class Replace(val replacement: Any): InterceptorResult
}