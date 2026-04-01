// Generated from /root/ZeroStudio/editor/lexers/src/main/antlr/smali/Smali.g4 by ANTLR 4.13.1
package com.itsaky.androidide.lexers.smali;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SmaliParser}.
 */
public interface SmaliListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SmaliParser#smali}.
	 * @param ctx the parse tree
	 */
	void enterSmali(SmaliParser.SmaliContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#smali}.
	 * @param ctx the parse tree
	 */
	void exitSmali(SmaliParser.SmaliContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ClassAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterClassAlt(SmaliParser.ClassAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ClassAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitClassAlt(SmaliParser.ClassAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SuperAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterSuperAlt(SmaliParser.SuperAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SuperAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitSuperAlt(SmaliParser.SuperAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SourceFileAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterSourceFileAlt(SmaliParser.SourceFileAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SourceFileAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitSourceFileAlt(SmaliParser.SourceFileAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ImplementsAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterImplementsAlt(SmaliParser.ImplementsAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ImplementsAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitImplementsAlt(SmaliParser.ImplementsAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ClassAnnotationAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterClassAnnotationAlt(SmaliParser.ClassAnnotationAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ClassAnnotationAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitClassAnnotationAlt(SmaliParser.ClassAnnotationAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterFieldAlt(SmaliParser.FieldAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitFieldAlt(SmaliParser.FieldAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void enterMethodAlt(SmaliParser.MethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodAlt}
	 * labeled alternative in {@link SmaliParser#member}.
	 * @param ctx the parse tree
	 */
	void exitMethodAlt(SmaliParser.MethodAltContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#classSpec}.
	 * @param ctx the parse tree
	 */
	void enterClassSpec(SmaliParser.ClassSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#classSpec}.
	 * @param ctx the parse tree
	 */
	void exitClassSpec(SmaliParser.ClassSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#accessList}.
	 * @param ctx the parse tree
	 */
	void enterAccessList(SmaliParser.AccessListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#accessList}.
	 * @param ctx the parse tree
	 */
	void exitAccessList(SmaliParser.AccessListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#superSpec}.
	 * @param ctx the parse tree
	 */
	void enterSuperSpec(SmaliParser.SuperSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#superSpec}.
	 * @param ctx the parse tree
	 */
	void exitSuperSpec(SmaliParser.SuperSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#sourceSpec}.
	 * @param ctx the parse tree
	 */
	void enterSourceSpec(SmaliParser.SourceSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#sourceSpec}.
	 * @param ctx the parse tree
	 */
	void exitSourceSpec(SmaliParser.SourceSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#implementsSpec}.
	 * @param ctx the parse tree
	 */
	void enterImplementsSpec(SmaliParser.ImplementsSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#implementsSpec}.
	 * @param ctx the parse tree
	 */
	void exitImplementsSpec(SmaliParser.ImplementsSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(SmaliParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(SmaliParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#annotation_element}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation_element(SmaliParser.Annotation_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#annotation_element}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation_element(SmaliParser.Annotation_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#subannotation}.
	 * @param ctx the parse tree
	 */
	void enterSubannotation(SmaliParser.SubannotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#subannotation}.
	 * @param ctx the parse tree
	 */
	void exitSubannotation(SmaliParser.SubannotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#simpleName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleName(SmaliParser.SimpleNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#simpleName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleName(SmaliParser.SimpleNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#memberName}.
	 * @param ctx the parse tree
	 */
	void enterMemberName(SmaliParser.MemberNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#memberName}.
	 * @param ctx the parse tree
	 */
	void exitMemberName(SmaliParser.MemberNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LongLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLongLiteralAlt(SmaliParser.LongLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LongLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLongLiteralAlt(SmaliParser.LongLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntegerLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteralAlt(SmaliParser.IntegerLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntegerLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteralAlt(SmaliParser.IntegerLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ShortLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterShortLiteralAlt(SmaliParser.ShortLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ShortLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitShortLiteralAlt(SmaliParser.ShortLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ByteLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterByteLiteralAlt(SmaliParser.ByteLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ByteLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitByteLiteralAlt(SmaliParser.ByteLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FloatLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteralAlt(SmaliParser.FloatLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FloatLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteralAlt(SmaliParser.FloatLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DoubleLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterDoubleLiteralAlt(SmaliParser.DoubleLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DoubleLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitDoubleLiteralAlt(SmaliParser.DoubleLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CharLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterCharLiteralAlt(SmaliParser.CharLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CharLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitCharLiteralAlt(SmaliParser.CharLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteralAlt(SmaliParser.StringLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteralAlt(SmaliParser.StringLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BooleanLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteralAlt(SmaliParser.BooleanLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BooleanLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteralAlt(SmaliParser.BooleanLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NullLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteralAlt(SmaliParser.NullLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NullLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteralAlt(SmaliParser.NullLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterArrayLiteralAlt(SmaliParser.ArrayLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitArrayLiteralAlt(SmaliParser.ArrayLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SubAnnotationLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterSubAnnotationLiteralAlt(SmaliParser.SubAnnotationLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SubAnnotationLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitSubAnnotationLiteralAlt(SmaliParser.SubAnnotationLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TypeLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterTypeLiteralAlt(SmaliParser.TypeLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TypeLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitTypeLiteralAlt(SmaliParser.TypeLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterFieldLiteralAlt(SmaliParser.FieldLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitFieldLiteralAlt(SmaliParser.FieldLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterMethodLiteralAlt(SmaliParser.MethodLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitMethodLiteralAlt(SmaliParser.MethodLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EnumLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterEnumLiteralAlt(SmaliParser.EnumLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EnumLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitEnumLiteralAlt(SmaliParser.EnumLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodHandleLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterMethodHandleLiteralAlt(SmaliParser.MethodHandleLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodHandleLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitMethodHandleLiteralAlt(SmaliParser.MethodHandleLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodProtoLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterMethodProtoLiteralAlt(SmaliParser.MethodProtoLiteralAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodProtoLiteralAlt}
	 * labeled alternative in {@link SmaliParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitMethodProtoLiteralAlt(SmaliParser.MethodProtoLiteralAltContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(SmaliParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(SmaliParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(SmaliParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#floatLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(SmaliParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#doubleLiteral}.
	 * @param ctx the parse tree
	 */
	void enterDoubleLiteral(SmaliParser.DoubleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#doubleLiteral}.
	 * @param ctx the parse tree
	 */
	void exitDoubleLiteral(SmaliParser.DoubleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void enterArrayLiteral(SmaliParser.ArrayLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void exitArrayLiteral(SmaliParser.ArrayLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#arrayMember}.
	 * @param ctx the parse tree
	 */
	void enterArrayMember(SmaliParser.ArrayMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#arrayMember}.
	 * @param ctx the parse tree
	 */
	void exitArrayMember(SmaliParser.ArrayMemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#typeLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTypeLiteral(SmaliParser.TypeLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#typeLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTypeLiteral(SmaliParser.TypeLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#fieldLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFieldLiteral(SmaliParser.FieldLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#fieldLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFieldLiteral(SmaliParser.FieldLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#fieldReference}.
	 * @param ctx the parse tree
	 */
	void enterFieldReference(SmaliParser.FieldReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#fieldReference}.
	 * @param ctx the parse tree
	 */
	void exitFieldReference(SmaliParser.FieldReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#methodLiteral}.
	 * @param ctx the parse tree
	 */
	void enterMethodLiteral(SmaliParser.MethodLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#methodLiteral}.
	 * @param ctx the parse tree
	 */
	void exitMethodLiteral(SmaliParser.MethodLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#methodReference}.
	 * @param ctx the parse tree
	 */
	void enterMethodReference(SmaliParser.MethodReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#methodReference}.
	 * @param ctx the parse tree
	 */
	void exitMethodReference(SmaliParser.MethodReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#methodProto}.
	 * @param ctx the parse tree
	 */
	void enterMethodProto(SmaliParser.MethodProtoContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#methodProto}.
	 * @param ctx the parse tree
	 */
	void exitMethodProto(SmaliParser.MethodProtoContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#type_descriptor}.
	 * @param ctx the parse tree
	 */
	void enterType_descriptor(SmaliParser.Type_descriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#type_descriptor}.
	 * @param ctx the parse tree
	 */
	void exitType_descriptor(SmaliParser.Type_descriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(SmaliParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(SmaliParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TypeAlt}
	 * labeled alternative in {@link SmaliParser#paramArg}.
	 * @param ctx the parse tree
	 */
	void enterTypeAlt(SmaliParser.TypeAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TypeAlt}
	 * labeled alternative in {@link SmaliParser#paramArg}.
	 * @param ctx the parse tree
	 */
	void exitTypeAlt(SmaliParser.TypeAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TypeListAlt}
	 * labeled alternative in {@link SmaliParser#paramArg}.
	 * @param ctx the parse tree
	 */
	void enterTypeListAlt(SmaliParser.TypeListAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TypeListAlt}
	 * labeled alternative in {@link SmaliParser#paramArg}.
	 * @param ctx the parse tree
	 */
	void exitTypeListAlt(SmaliParser.TypeListAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PermitiveList}
	 * labeled alternative in {@link SmaliParser#paramArg}.
	 * @param ctx the parse tree
	 */
	void enterPermitiveList(SmaliParser.PermitiveListContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PermitiveList}
	 * labeled alternative in {@link SmaliParser#paramArg}.
	 * @param ctx the parse tree
	 */
	void exitPermitiveList(SmaliParser.PermitiveListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#reference_type_descriptor}.
	 * @param ctx the parse tree
	 */
	void enterReference_type_descriptor(SmaliParser.Reference_type_descriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#reference_type_descriptor}.
	 * @param ctx the parse tree
	 */
	void exitReference_type_descriptor(SmaliParser.Reference_type_descriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#nonvoid_type_descriptor}.
	 * @param ctx the parse tree
	 */
	void enterNonvoid_type_descriptor(SmaliParser.Nonvoid_type_descriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#nonvoid_type_descriptor}.
	 * @param ctx the parse tree
	 */
	void exitNonvoid_type_descriptor(SmaliParser.Nonvoid_type_descriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#enumLiteral}.
	 * @param ctx the parse tree
	 */
	void enterEnumLiteral(SmaliParser.EnumLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#enumLiteral}.
	 * @param ctx the parse tree
	 */
	void exitEnumLiteral(SmaliParser.EnumLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#methodHandleLiteral}.
	 * @param ctx the parse tree
	 */
	void enterMethodHandleLiteral(SmaliParser.MethodHandleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#methodHandleLiteral}.
	 * @param ctx the parse tree
	 */
	void exitMethodHandleLiteral(SmaliParser.MethodHandleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#methodHandleReference}.
	 * @param ctx the parse tree
	 */
	void enterMethodHandleReference(SmaliParser.MethodHandleReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#methodHandleReference}.
	 * @param ctx the parse tree
	 */
	void exitMethodHandleReference(SmaliParser.MethodHandleReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(SmaliParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(SmaliParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#method}.
	 * @param ctx the parse tree
	 */
	void enterMethod(SmaliParser.MethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#method}.
	 * @param ctx the parse tree
	 */
	void exitMethod(SmaliParser.MethodContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RegisterAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterRegisterAlt(SmaliParser.RegisterAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RegisterAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitRegisterAlt(SmaliParser.RegisterAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CatchAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterCatchAlt(SmaliParser.CatchAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CatchAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitCatchAlt(SmaliParser.CatchAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CatchAllAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterCatchAllAlt(SmaliParser.CatchAllAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CatchAllAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitCatchAllAlt(SmaliParser.CatchAllAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParamterAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterParamterAlt(SmaliParser.ParamterAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParamterAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitParamterAlt(SmaliParser.ParamterAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodAnnotationAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterMethodAnnotationAlt(SmaliParser.MethodAnnotationAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodAnnotationAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitMethodAnnotationAlt(SmaliParser.MethodAnnotationAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LableAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterLableAlt(SmaliParser.LableAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LableAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitLableAlt(SmaliParser.LableAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LineAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterLineAlt(SmaliParser.LineAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LineAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitLineAlt(SmaliParser.LineAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LocalAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterLocalAlt(SmaliParser.LocalAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LocalAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitLocalAlt(SmaliParser.LocalAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EndLocalAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterEndLocalAlt(SmaliParser.EndLocalAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EndLocalAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitEndLocalAlt(SmaliParser.EndLocalAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RestartLocalAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterRestartLocalAlt(SmaliParser.RestartLocalAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RestartLocalAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitRestartLocalAlt(SmaliParser.RestartLocalAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PrologueAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterPrologueAlt(SmaliParser.PrologueAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PrologueAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitPrologueAlt(SmaliParser.PrologueAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EpilogueAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterEpilogueAlt(SmaliParser.EpilogueAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EpilogueAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitEpilogueAlt(SmaliParser.EpilogueAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SourceAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterSourceAlt(SmaliParser.SourceAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SourceAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitSourceAlt(SmaliParser.SourceAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction10tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction10tAlt(SmaliParser.Instruction10tAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction10tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction10tAlt(SmaliParser.Instruction10tAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction10xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction10xAlt(SmaliParser.Instruction10xAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction10xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction10xAlt(SmaliParser.Instruction10xAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction10xOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction10xOdexAlt(SmaliParser.Instruction10xOdexAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction10xOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction10xOdexAlt(SmaliParser.Instruction10xOdexAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction11nAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction11nAlt(SmaliParser.Instruction11nAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction11nAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction11nAlt(SmaliParser.Instruction11nAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction11xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction11xAlt(SmaliParser.Instruction11xAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction11xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction11xAlt(SmaliParser.Instruction11xAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction12xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction12xAlt(SmaliParser.Instruction12xAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction12xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction12xAlt(SmaliParser.Instruction12xAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction20bcAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction20bcAlt(SmaliParser.Instruction20bcAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction20bcAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction20bcAlt(SmaliParser.Instruction20bcAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction20tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction20tAlt(SmaliParser.Instruction20tAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction20tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction20tAlt(SmaliParser.Instruction20tAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21cFieldAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21cFieldAlt(SmaliParser.Instruction21cFieldAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21cFieldAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21cFieldAlt(SmaliParser.Instruction21cFieldAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21cFieldOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21cFieldOdexAlt(SmaliParser.Instruction21cFieldOdexAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21cFieldOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21cFieldOdexAlt(SmaliParser.Instruction21cFieldOdexAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21cMethodHandleAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21cMethodHandleAlt(SmaliParser.Instruction21cMethodHandleAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21cMethodHandleAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21cMethodHandleAlt(SmaliParser.Instruction21cMethodHandleAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21cMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21cMethodAlt(SmaliParser.Instruction21cMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21cMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21cMethodAlt(SmaliParser.Instruction21cMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21cStringAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21cStringAlt(SmaliParser.Instruction21cStringAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21cStringAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21cStringAlt(SmaliParser.Instruction21cStringAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21cTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21cTypeAlt(SmaliParser.Instruction21cTypeAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21cTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21cTypeAlt(SmaliParser.Instruction21cTypeAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21ihAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21ihAlt(SmaliParser.Instruction21ihAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21ihAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21ihAlt(SmaliParser.Instruction21ihAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21lhAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21lhAlt(SmaliParser.Instruction21lhAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21lhAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21lhAlt(SmaliParser.Instruction21lhAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21sAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21sAlt(SmaliParser.Instruction21sAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21sAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21sAlt(SmaliParser.Instruction21sAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction21tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction21tAlt(SmaliParser.Instruction21tAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction21tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction21tAlt(SmaliParser.Instruction21tAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22bAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22bAlt(SmaliParser.Instruction22bAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22bAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22bAlt(SmaliParser.Instruction22bAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22cFieldAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22cFieldAlt(SmaliParser.Instruction22cFieldAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22cFieldAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22cFieldAlt(SmaliParser.Instruction22cFieldAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22cFieldOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22cFieldOdexAlt(SmaliParser.Instruction22cFieldOdexAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22cFieldOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22cFieldOdexAlt(SmaliParser.Instruction22cFieldOdexAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22cTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22cTypeAlt(SmaliParser.Instruction22cTypeAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22cTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22cTypeAlt(SmaliParser.Instruction22cTypeAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22csFieldAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22csFieldAlt(SmaliParser.Instruction22csFieldAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22csFieldAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22csFieldAlt(SmaliParser.Instruction22csFieldAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22sAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22sAlt(SmaliParser.Instruction22sAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22sAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22sAlt(SmaliParser.Instruction22sAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22tAlt(SmaliParser.Instruction22tAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22tAlt(SmaliParser.Instruction22tAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction22xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction22xAlt(SmaliParser.Instruction22xAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction22xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction22xAlt(SmaliParser.Instruction22xAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction23xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction23xAlt(SmaliParser.Instruction23xAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction23xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction23xAlt(SmaliParser.Instruction23xAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction30tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction30tAlt(SmaliParser.Instruction30tAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction30tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction30tAlt(SmaliParser.Instruction30tAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction31cAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction31cAlt(SmaliParser.Instruction31cAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction31cAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction31cAlt(SmaliParser.Instruction31cAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction31iAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction31iAlt(SmaliParser.Instruction31iAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction31iAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction31iAlt(SmaliParser.Instruction31iAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction31tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction31tAlt(SmaliParser.Instruction31tAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction31tAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction31tAlt(SmaliParser.Instruction31tAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction32xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction32xAlt(SmaliParser.Instruction32xAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction32xAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction32xAlt(SmaliParser.Instruction32xAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction35cCallsiteAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction35cCallsiteAlt(SmaliParser.Instruction35cCallsiteAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction35cCallsiteAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction35cCallsiteAlt(SmaliParser.Instruction35cCallsiteAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction35cMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction35cMethodAlt(SmaliParser.Instruction35cMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction35cMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction35cMethodAlt(SmaliParser.Instruction35cMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction35cTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction35cTypeAlt(SmaliParser.Instruction35cTypeAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction35cTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction35cTypeAlt(SmaliParser.Instruction35cTypeAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction35cMethodOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction35cMethodOdexAlt(SmaliParser.Instruction35cMethodOdexAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction35cMethodOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction35cMethodOdexAlt(SmaliParser.Instruction35cMethodOdexAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction35miMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction35miMethodAlt(SmaliParser.Instruction35miMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction35miMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction35miMethodAlt(SmaliParser.Instruction35miMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction35msMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction35msMethodAlt(SmaliParser.Instruction35msMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction35msMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction35msMethodAlt(SmaliParser.Instruction35msMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction3rcCallsiteAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction3rcCallsiteAlt(SmaliParser.Instruction3rcCallsiteAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction3rcCallsiteAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction3rcCallsiteAlt(SmaliParser.Instruction3rcCallsiteAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction3rcMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction3rcMethodAlt(SmaliParser.Instruction3rcMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction3rcMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction3rcMethodAlt(SmaliParser.Instruction3rcMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction3rcMethodOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction3rcMethodOdexAlt(SmaliParser.Instruction3rcMethodOdexAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction3rcMethodOdexAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction3rcMethodOdexAlt(SmaliParser.Instruction3rcMethodOdexAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction3rcTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction3rcTypeAlt(SmaliParser.Instruction3rcTypeAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction3rcTypeAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction3rcTypeAlt(SmaliParser.Instruction3rcTypeAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction3rmiMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction3rmiMethodAlt(SmaliParser.Instruction3rmiMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction3rmiMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction3rmiMethodAlt(SmaliParser.Instruction3rmiMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction3rmsMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction3rmsMethodAlt(SmaliParser.Instruction3rmsMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction3rmsMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction3rmsMethodAlt(SmaliParser.Instruction3rmsMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction45ccMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction45ccMethodAlt(SmaliParser.Instruction45ccMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction45ccMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction45ccMethodAlt(SmaliParser.Instruction45ccMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction4rccMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction4rccMethodAlt(SmaliParser.Instruction4rccMethodAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction4rccMethodAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction4rccMethodAlt(SmaliParser.Instruction4rccMethodAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Instruction51lAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstruction51lAlt(SmaliParser.Instruction51lAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Instruction51lAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstruction51lAlt(SmaliParser.Instruction51lAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InstructionArraydataAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstructionArraydataAlt(SmaliParser.InstructionArraydataAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InstructionArraydataAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstructionArraydataAlt(SmaliParser.InstructionArraydataAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InstructionPackedSwitchAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstructionPackedSwitchAlt(SmaliParser.InstructionPackedSwitchAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InstructionPackedSwitchAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstructionPackedSwitchAlt(SmaliParser.InstructionPackedSwitchAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InstructionSparseSwitchAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void enterInstructionSparseSwitchAlt(SmaliParser.InstructionSparseSwitchAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InstructionSparseSwitchAlt}
	 * labeled alternative in {@link SmaliParser#statement_or_directive}.
	 * @param ctx the parse tree
	 */
	void exitInstructionSparseSwitchAlt(SmaliParser.InstructionSparseSwitchAltContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#registersDirective}.
	 * @param ctx the parse tree
	 */
	void enterRegistersDirective(SmaliParser.RegistersDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#registersDirective}.
	 * @param ctx the parse tree
	 */
	void exitRegistersDirective(SmaliParser.RegistersDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#catchDirective}.
	 * @param ctx the parse tree
	 */
	void enterCatchDirective(SmaliParser.CatchDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#catchDirective}.
	 * @param ctx the parse tree
	 */
	void exitCatchDirective(SmaliParser.CatchDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#catchallDirective}.
	 * @param ctx the parse tree
	 */
	void enterCatchallDirective(SmaliParser.CatchallDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#catchallDirective}.
	 * @param ctx the parse tree
	 */
	void exitCatchallDirective(SmaliParser.CatchallDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#parameterDirective}.
	 * @param ctx the parse tree
	 */
	void enterParameterDirective(SmaliParser.ParameterDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#parameterDirective}.
	 * @param ctx the parse tree
	 */
	void exitParameterDirective(SmaliParser.ParameterDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#integralLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegralLiteral(SmaliParser.IntegralLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#integralLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegralLiteral(SmaliParser.IntegralLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#label_ref}.
	 * @param ctx the parse tree
	 */
	void enterLabel_ref(SmaliParser.Label_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#label_ref}.
	 * @param ctx the parse tree
	 */
	void exitLabel_ref(SmaliParser.Label_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(SmaliParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(SmaliParser.LabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#lineDirective}.
	 * @param ctx the parse tree
	 */
	void enterLineDirective(SmaliParser.LineDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#lineDirective}.
	 * @param ctx the parse tree
	 */
	void exitLineDirective(SmaliParser.LineDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#localDirective}.
	 * @param ctx the parse tree
	 */
	void enterLocalDirective(SmaliParser.LocalDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#localDirective}.
	 * @param ctx the parse tree
	 */
	void exitLocalDirective(SmaliParser.LocalDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#endLocalDirective}.
	 * @param ctx the parse tree
	 */
	void enterEndLocalDirective(SmaliParser.EndLocalDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#endLocalDirective}.
	 * @param ctx the parse tree
	 */
	void exitEndLocalDirective(SmaliParser.EndLocalDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#restartLocalDirective}.
	 * @param ctx the parse tree
	 */
	void enterRestartLocalDirective(SmaliParser.RestartLocalDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#restartLocalDirective}.
	 * @param ctx the parse tree
	 */
	void exitRestartLocalDirective(SmaliParser.RestartLocalDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#sourceDirective}.
	 * @param ctx the parse tree
	 */
	void enterSourceDirective(SmaliParser.SourceDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#sourceDirective}.
	 * @param ctx the parse tree
	 */
	void exitSourceDirective(SmaliParser.SourceDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#instruction_format12x}.
	 * @param ctx the parse tree
	 */
	void enterInstruction_format12x(SmaliParser.Instruction_format12xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#instruction_format12x}.
	 * @param ctx the parse tree
	 */
	void exitInstruction_format12x(SmaliParser.Instruction_format12xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#instruction_format22s}.
	 * @param ctx the parse tree
	 */
	void enterInstruction_format22s(SmaliParser.Instruction_format22sContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#instruction_format22s}.
	 * @param ctx the parse tree
	 */
	void exitInstruction_format22s(SmaliParser.Instruction_format22sContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#instruction_format31i}.
	 * @param ctx the parse tree
	 */
	void enterInstruction_format31i(SmaliParser.Instruction_format31iContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#instruction_format31i}.
	 * @param ctx the parse tree
	 */
	void exitInstruction_format31i(SmaliParser.Instruction_format31iContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#instruction_format35c_method}.
	 * @param ctx the parse tree
	 */
	void enterInstruction_format35c_method(SmaliParser.Instruction_format35c_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#instruction_format35c_method}.
	 * @param ctx the parse tree
	 */
	void exitInstruction_format35c_method(SmaliParser.Instruction_format35c_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format10t}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format10t(SmaliParser.Insn_format10tContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format10t}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format10t(SmaliParser.Insn_format10tContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format10x}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format10x(SmaliParser.Insn_format10xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format10x}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format10x(SmaliParser.Insn_format10xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format10x_odex}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format10x_odex(SmaliParser.Insn_format10x_odexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format10x_odex}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format10x_odex(SmaliParser.Insn_format10x_odexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format11n}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format11n(SmaliParser.Insn_format11nContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format11n}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format11n(SmaliParser.Insn_format11nContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format11x}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format11x(SmaliParser.Insn_format11xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format11x}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format11x(SmaliParser.Insn_format11xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format12x}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format12x(SmaliParser.Insn_format12xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format12x}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format12x(SmaliParser.Insn_format12xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format20bc}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format20bc(SmaliParser.Insn_format20bcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format20bc}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format20bc(SmaliParser.Insn_format20bcContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format20t}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format20t(SmaliParser.Insn_format20tContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format20t}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format20t(SmaliParser.Insn_format20tContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21c_field}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21c_field(SmaliParser.Insn_format21c_fieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21c_field}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21c_field(SmaliParser.Insn_format21c_fieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21c_field_odex}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21c_field_odex(SmaliParser.Insn_format21c_field_odexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21c_field_odex}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21c_field_odex(SmaliParser.Insn_format21c_field_odexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21c_method_handle}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21c_method_handle(SmaliParser.Insn_format21c_method_handleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21c_method_handle}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21c_method_handle(SmaliParser.Insn_format21c_method_handleContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21c_method_type}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21c_method_type(SmaliParser.Insn_format21c_method_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21c_method_type}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21c_method_type(SmaliParser.Insn_format21c_method_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21c_string}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21c_string(SmaliParser.Insn_format21c_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21c_string}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21c_string(SmaliParser.Insn_format21c_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21c_type}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21c_type(SmaliParser.Insn_format21c_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21c_type}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21c_type(SmaliParser.Insn_format21c_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21ih}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21ih(SmaliParser.Insn_format21ihContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21ih}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21ih(SmaliParser.Insn_format21ihContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21lh}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21lh(SmaliParser.Insn_format21lhContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21lh}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21lh(SmaliParser.Insn_format21lhContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21s}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21s(SmaliParser.Insn_format21sContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21s}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21s(SmaliParser.Insn_format21sContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format21t}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format21t(SmaliParser.Insn_format21tContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format21t}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format21t(SmaliParser.Insn_format21tContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22b}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22b(SmaliParser.Insn_format22bContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22b}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22b(SmaliParser.Insn_format22bContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22c_field}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22c_field(SmaliParser.Insn_format22c_fieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22c_field}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22c_field(SmaliParser.Insn_format22c_fieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22c_field_odex}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22c_field_odex(SmaliParser.Insn_format22c_field_odexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22c_field_odex}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22c_field_odex(SmaliParser.Insn_format22c_field_odexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22c_type}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22c_type(SmaliParser.Insn_format22c_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22c_type}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22c_type(SmaliParser.Insn_format22c_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22cs_field}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22cs_field(SmaliParser.Insn_format22cs_fieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22cs_field}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22cs_field(SmaliParser.Insn_format22cs_fieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22s}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22s(SmaliParser.Insn_format22sContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22s}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22s(SmaliParser.Insn_format22sContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22t}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22t(SmaliParser.Insn_format22tContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22t}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22t(SmaliParser.Insn_format22tContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format22x}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format22x(SmaliParser.Insn_format22xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format22x}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format22x(SmaliParser.Insn_format22xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format23x}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format23x(SmaliParser.Insn_format23xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format23x}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format23x(SmaliParser.Insn_format23xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format30t}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format30t(SmaliParser.Insn_format30tContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format30t}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format30t(SmaliParser.Insn_format30tContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format31c}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format31c(SmaliParser.Insn_format31cContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format31c}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format31c(SmaliParser.Insn_format31cContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format31i}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format31i(SmaliParser.Insn_format31iContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format31i}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format31i(SmaliParser.Insn_format31iContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format31t}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format31t(SmaliParser.Insn_format31tContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format31t}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format31t(SmaliParser.Insn_format31tContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format32x}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format32x(SmaliParser.Insn_format32xContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format32x}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format32x(SmaliParser.Insn_format32xContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format35c_call_site}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format35c_call_site(SmaliParser.Insn_format35c_call_siteContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format35c_call_site}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format35c_call_site(SmaliParser.Insn_format35c_call_siteContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format35c_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format35c_method(SmaliParser.Insn_format35c_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format35c_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format35c_method(SmaliParser.Insn_format35c_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format35c_type}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format35c_type(SmaliParser.Insn_format35c_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format35c_type}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format35c_type(SmaliParser.Insn_format35c_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format35c_method_odex}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format35c_method_odex(SmaliParser.Insn_format35c_method_odexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format35c_method_odex}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format35c_method_odex(SmaliParser.Insn_format35c_method_odexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format35mi_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format35mi_method(SmaliParser.Insn_format35mi_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format35mi_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format35mi_method(SmaliParser.Insn_format35mi_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format35ms_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format35ms_method(SmaliParser.Insn_format35ms_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format35ms_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format35ms_method(SmaliParser.Insn_format35ms_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format3rc_call_site}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format3rc_call_site(SmaliParser.Insn_format3rc_call_siteContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format3rc_call_site}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format3rc_call_site(SmaliParser.Insn_format3rc_call_siteContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format3rc_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format3rc_method(SmaliParser.Insn_format3rc_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format3rc_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format3rc_method(SmaliParser.Insn_format3rc_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format3rc_method_odex}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format3rc_method_odex(SmaliParser.Insn_format3rc_method_odexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format3rc_method_odex}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format3rc_method_odex(SmaliParser.Insn_format3rc_method_odexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format3rc_type}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format3rc_type(SmaliParser.Insn_format3rc_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format3rc_type}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format3rc_type(SmaliParser.Insn_format3rc_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format3rmi_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format3rmi_method(SmaliParser.Insn_format3rmi_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format3rmi_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format3rmi_method(SmaliParser.Insn_format3rmi_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format3rms_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format3rms_method(SmaliParser.Insn_format3rms_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format3rms_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format3rms_method(SmaliParser.Insn_format3rms_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format45cc_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format45cc_method(SmaliParser.Insn_format45cc_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format45cc_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format45cc_method(SmaliParser.Insn_format45cc_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format4rcc_method}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format4rcc_method(SmaliParser.Insn_format4rcc_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format4rcc_method}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format4rcc_method(SmaliParser.Insn_format4rcc_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_format51l}.
	 * @param ctx the parse tree
	 */
	void enterInsn_format51l(SmaliParser.Insn_format51lContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_format51l}.
	 * @param ctx the parse tree
	 */
	void exitInsn_format51l(SmaliParser.Insn_format51lContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_array_data_directive}.
	 * @param ctx the parse tree
	 */
	void enterInsn_array_data_directive(SmaliParser.Insn_array_data_directiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_array_data_directive}.
	 * @param ctx the parse tree
	 */
	void exitInsn_array_data_directive(SmaliParser.Insn_array_data_directiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_packed_switch_directive}.
	 * @param ctx the parse tree
	 */
	void enterInsn_packed_switch_directive(SmaliParser.Insn_packed_switch_directiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_packed_switch_directive}.
	 * @param ctx the parse tree
	 */
	void exitInsn_packed_switch_directive(SmaliParser.Insn_packed_switch_directiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#insn_sparse_switch_directive}.
	 * @param ctx the parse tree
	 */
	void enterInsn_sparse_switch_directive(SmaliParser.Insn_sparse_switch_directiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#insn_sparse_switch_directive}.
	 * @param ctx the parse tree
	 */
	void exitInsn_sparse_switch_directive(SmaliParser.Insn_sparse_switch_directiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#switchElement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchElement(SmaliParser.SwitchElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#switchElement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchElement(SmaliParser.SwitchElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#register_list}.
	 * @param ctx the parse tree
	 */
	void enterRegister_list(SmaliParser.Register_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#register_list}.
	 * @param ctx the parse tree
	 */
	void exitRegister_list(SmaliParser.Register_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#register_range}.
	 * @param ctx the parse tree
	 */
	void enterRegister_range(SmaliParser.Register_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#register_range}.
	 * @param ctx the parse tree
	 */
	void exitRegister_range(SmaliParser.Register_rangeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TypeErrorAlt}
	 * labeled alternative in {@link SmaliParser#verification_error_reference}.
	 * @param ctx the parse tree
	 */
	void enterTypeErrorAlt(SmaliParser.TypeErrorAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TypeErrorAlt}
	 * labeled alternative in {@link SmaliParser#verification_error_reference}.
	 * @param ctx the parse tree
	 */
	void exitTypeErrorAlt(SmaliParser.TypeErrorAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldErrorAlt}
	 * labeled alternative in {@link SmaliParser#verification_error_reference}.
	 * @param ctx the parse tree
	 */
	void enterFieldErrorAlt(SmaliParser.FieldErrorAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldErrorAlt}
	 * labeled alternative in {@link SmaliParser#verification_error_reference}.
	 * @param ctx the parse tree
	 */
	void exitFieldErrorAlt(SmaliParser.FieldErrorAltContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodErrotAlt}
	 * labeled alternative in {@link SmaliParser#verification_error_reference}.
	 * @param ctx the parse tree
	 */
	void enterMethodErrotAlt(SmaliParser.MethodErrotAltContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodErrotAlt}
	 * labeled alternative in {@link SmaliParser#verification_error_reference}.
	 * @param ctx the parse tree
	 */
	void exitMethodErrotAlt(SmaliParser.MethodErrotAltContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#callsiteReference}.
	 * @param ctx the parse tree
	 */
	void enterCallsiteReference(SmaliParser.CallsiteReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#callsiteReference}.
	 * @param ctx the parse tree
	 */
	void exitCallsiteReference(SmaliParser.CallsiteReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#fixed32bitLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFixed32bitLiteral(SmaliParser.Fixed32bitLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#fixed32bitLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFixed32bitLiteral(SmaliParser.Fixed32bitLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link SmaliParser#fixed64bitLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFixed64bitLiteral(SmaliParser.Fixed64bitLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SmaliParser#fixed64bitLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFixed64bitLiteral(SmaliParser.Fixed64bitLiteralContext ctx);
}