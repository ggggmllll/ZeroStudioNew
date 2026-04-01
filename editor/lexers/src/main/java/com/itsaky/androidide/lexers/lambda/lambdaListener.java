// Generated from /root/ZeroStudio/editor/lexers/src/main/antlr/lambda/lambda.g4 by ANTLR 4.13.1
package com.itsaky.androidide.lexers.lambda;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link lambdaParser}.
 */
public interface lambdaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link lambdaParser#file_}.
	 * @param ctx the parse tree
	 */
	void enterFile_(lambdaParser.File_Context ctx);
	/**
	 * Exit a parse tree produced by {@link lambdaParser#file_}.
	 * @param ctx the parse tree
	 */
	void exitFile_(lambdaParser.File_Context ctx);
	/**
	 * Enter a parse tree produced by {@link lambdaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(lambdaParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link lambdaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(lambdaParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link lambdaParser#function_}.
	 * @param ctx the parse tree
	 */
	void enterFunction_(lambdaParser.Function_Context ctx);
	/**
	 * Exit a parse tree produced by {@link lambdaParser#function_}.
	 * @param ctx the parse tree
	 */
	void exitFunction_(lambdaParser.Function_Context ctx);
	/**
	 * Enter a parse tree produced by {@link lambdaParser#application}.
	 * @param ctx the parse tree
	 */
	void enterApplication(lambdaParser.ApplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link lambdaParser#application}.
	 * @param ctx the parse tree
	 */
	void exitApplication(lambdaParser.ApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link lambdaParser#scope}.
	 * @param ctx the parse tree
	 */
	void enterScope(lambdaParser.ScopeContext ctx);
	/**
	 * Exit a parse tree produced by {@link lambdaParser#scope}.
	 * @param ctx the parse tree
	 */
	void exitScope(lambdaParser.ScopeContext ctx);
}