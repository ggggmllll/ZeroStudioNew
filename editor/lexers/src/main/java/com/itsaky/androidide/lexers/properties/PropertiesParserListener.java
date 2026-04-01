// Generated from /root/ZeroStudio/editor/lexers/src/main/antlr/properties/PropertiesParser.g4 by ANTLR 4.13.1
package com.itsaky.androidide.lexers.properties;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PropertiesParser}.
 */
public interface PropertiesParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#propertiesFile}.
	 * @param ctx the parse tree
	 */
	void enterPropertiesFile(PropertiesParser.PropertiesFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#propertiesFile}.
	 * @param ctx the parse tree
	 */
	void exitPropertiesFile(PropertiesParser.PropertiesFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#row}.
	 * @param ctx the parse tree
	 */
	void enterRow(PropertiesParser.RowContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#row}.
	 * @param ctx the parse tree
	 */
	void exitRow(PropertiesParser.RowContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#line}.
	 * @param ctx the parse tree
	 */
	void enterLine(PropertiesParser.LineContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#line}.
	 * @param ctx the parse tree
	 */
	void exitLine(PropertiesParser.LineContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(PropertiesParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(PropertiesParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#eol}.
	 * @param ctx the parse tree
	 */
	void enterEol(PropertiesParser.EolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#eol}.
	 * @param ctx the parse tree
	 */
	void exitEol(PropertiesParser.EolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(PropertiesParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(PropertiesParser.CommentContext ctx);
}