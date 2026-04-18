package com.itsaky.androidide.lsp.rpc

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import java.lang.reflect.Type

/**
 * 负责将 LSP 数据模型与 Protobuf 消息进行互转
 * 支持对 Either 等泛型进行安全转换。
 */
object LspMessageConverter {
    private val gson: Gson = GsonBuilder().create()
    private val parser = JsonFormat.parser().ignoringUnknownFields()
    private val printer = JsonFormat.printer().omittingInsignificantWhitespace()

    /**
     * 将任意 POJO 转换为 Protobuf Value
     */
    fun toProtoValue(obj: Any?): Value {
        if (obj == null) return Value.newBuilder().setNullValue(com.google.protobuf.NullValue.NULL_VALUE).build()
        val json = gson.toJson(obj)
        val builder = Value.newBuilder()
        parser.merge(json, builder)
        return builder.build()
    }

    /**
     * 从 Protobuf Value 转换为指定的 Kotlin 类型
     */
    fun <T> fromProtoValue(value: Value, type: Type): T {
        val json = printer.print(value)
        return gson.fromJson(json, type)
    }

    /**
     * 针对 Either 类型的特殊处理（LSP 常用）
     */
    fun <T> fromProtoValue(value: Value, typeToken: TypeToken<T>): T {
        val json = printer.print(value)
        return gson.fromJson(json, typeToken.type)
    }
}