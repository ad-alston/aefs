// Generated from AccessPolicy.g by ANTLR 4.5.3

    package abe.policy;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link AccessPolicyParser}.
 */
public interface AccessPolicyListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link AccessPolicyParser#policy}.
	 * @param ctx the parse tree
	 */
	void enterPolicy(AccessPolicyParser.PolicyContext ctx);
	/**
	 * Exit a parse tree produced by {@link AccessPolicyParser#policy}.
	 * @param ctx the parse tree
	 */
	void exitPolicy(AccessPolicyParser.PolicyContext ctx);
	/**
	 * Enter a parse tree produced by {@link AccessPolicyParser#attribute_expression}.
	 * @param ctx the parse tree
	 */
	void enterAttribute_expression(AccessPolicyParser.Attribute_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AccessPolicyParser#attribute_expression}.
	 * @param ctx the parse tree
	 */
	void exitAttribute_expression(AccessPolicyParser.Attribute_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AccessPolicyParser#attribute_atom}.
	 * @param ctx the parse tree
	 */
	void enterAttribute_atom(AccessPolicyParser.Attribute_atomContext ctx);
	/**
	 * Exit a parse tree produced by {@link AccessPolicyParser#attribute_atom}.
	 * @param ctx the parse tree
	 */
	void exitAttribute_atom(AccessPolicyParser.Attribute_atomContext ctx);
}