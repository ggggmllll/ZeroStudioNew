// Generated from /root/ZeroStudio/editor/lexers/src/main/antlr/protobuf3/Protobuf3.g4 by ANTLR 4.13.1
package com.itsaky.androidide.lexers.protobuf3;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Protobuf3Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Protobuf3Visitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#proto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProto(Protobuf3Parser.ProtoContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#syntax}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSyntax(Protobuf3Parser.SyntaxContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#importStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportStatement(Protobuf3Parser.ImportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#packageStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageStatement(Protobuf3Parser.PackageStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#optionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionStatement(Protobuf3Parser.OptionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#optionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionName(Protobuf3Parser.OptionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#fieldLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldLabel(Protobuf3Parser.FieldLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(Protobuf3Parser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#fieldOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldOptions(Protobuf3Parser.FieldOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#fieldOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldOption(Protobuf3Parser.FieldOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#fieldNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldNumber(Protobuf3Parser.FieldNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#oneof}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOneof(Protobuf3Parser.OneofContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#oneofField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOneofField(Protobuf3Parser.OneofFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#mapField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapField(Protobuf3Parser.MapFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#keyType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyType(Protobuf3Parser.KeyTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#type_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_(Protobuf3Parser.Type_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#reserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReserved(Protobuf3Parser.ReservedContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#ranges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRanges(Protobuf3Parser.RangesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#range_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_(Protobuf3Parser.Range_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#reservedFieldNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReservedFieldNames(Protobuf3Parser.ReservedFieldNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#topLevelDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopLevelDef(Protobuf3Parser.TopLevelDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDef(Protobuf3Parser.EnumDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumBody(Protobuf3Parser.EnumBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumElement(Protobuf3Parser.EnumElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumField(Protobuf3Parser.EnumFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumValueOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValueOptions(Protobuf3Parser.EnumValueOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumValueOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValueOption(Protobuf3Parser.EnumValueOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#messageDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMessageDef(Protobuf3Parser.MessageDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#messageBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMessageBody(Protobuf3Parser.MessageBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#messageElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMessageElement(Protobuf3Parser.MessageElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#extendDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtendDef(Protobuf3Parser.ExtendDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#serviceDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceDef(Protobuf3Parser.ServiceDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#serviceElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceElement(Protobuf3Parser.ServiceElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#rpc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRpc(Protobuf3Parser.RpcContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(Protobuf3Parser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#blockLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockLit(Protobuf3Parser.BlockLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#emptyStatement_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement_(Protobuf3Parser.EmptyStatement_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdent(Protobuf3Parser.IdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#fullIdent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullIdent(Protobuf3Parser.FullIdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#messageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMessageName(Protobuf3Parser.MessageNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumName(Protobuf3Parser.EnumNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#fieldName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldName(Protobuf3Parser.FieldNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#oneofName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOneofName(Protobuf3Parser.OneofNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#mapName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapName(Protobuf3Parser.MapNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#serviceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceName(Protobuf3Parser.ServiceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#rpcName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRpcName(Protobuf3Parser.RpcNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#messageType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMessageType(Protobuf3Parser.MessageTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#enumType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumType(Protobuf3Parser.EnumTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#intLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLit(Protobuf3Parser.IntLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#strLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrLit(Protobuf3Parser.StrLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#boolLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolLit(Protobuf3Parser.BoolLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#floatLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLit(Protobuf3Parser.FloatLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link Protobuf3Parser#keywords}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywords(Protobuf3Parser.KeywordsContext ctx);
}