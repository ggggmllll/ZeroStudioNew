package com.itsaky.androidide.lsp.rpc

/**
 * 标注协议引入的版本
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtocolSince(val version: String)

/**
 * 标注协议已废弃的版本
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtocolDeprecated(val version: String)

/**
 * 标注当前协议仍处于草案阶段
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtocolDraft

/**
 * 标识这是一个 LSP 请求 (Request)，需要响应
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LspRequest(val method: String)

/**
 * 标识这是一个 LSP 通知 (Notification)，无需响应
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LspNotification(val method: String)