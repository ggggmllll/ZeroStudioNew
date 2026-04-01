// Generated from /root/ZeroStudio/editor/lexers/src/main/antlr/lambda/lambda.g4 by ANTLR 4.13.1
package com.itsaky.androidide.lexers.lambda;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link lambdaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface lambdaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link lambdaParser#file_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_(lambdaParser.File_Context ctx);
	/**
	 * Visit a parse tree produced by {@link lambdaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(lambdaParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link lambdaParser#function_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_(lambdaParser.Function_Context ctx);
	/**
	 * Visit a parse tree produced by {@link lambdaParser#application}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplication(lambdaParser.ApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link lambdaParser#scope}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScope(lambdaParser.ScopeContext ctx);
}