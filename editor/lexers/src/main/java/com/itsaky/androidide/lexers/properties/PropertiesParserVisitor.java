// Generated from /root/ZeroStudio/editor/lexers/src/main/antlr/properties/PropertiesParser.g4 by ANTLR 4.13.1
package com.itsaky.androidide.lexers.properties;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PropertiesParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PropertiesParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#propertiesFile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertiesFile(PropertiesParser.PropertiesFileContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(PropertiesParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLine(PropertiesParser.LineContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(PropertiesParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#eol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEol(PropertiesParser.EolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(PropertiesParser.CommentContext ctx);
}