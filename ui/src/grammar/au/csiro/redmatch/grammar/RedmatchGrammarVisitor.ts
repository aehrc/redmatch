// Generated from src/grammar/au/csiro/redmatch/grammar/RedmatchGrammar.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";

import { DocumentContext } from "./RedmatchGrammar";
import { FcRuleContext } from "./RedmatchGrammar";
import { FcBodyContext } from "./RedmatchGrammar";
import { RepeatsClauseContext } from "./RedmatchGrammar";
import { ConditionContext } from "./RedmatchGrammar";
import { ResourceContext } from "./RedmatchGrammar";
import { AttributeContext } from "./RedmatchGrammar";
import { ValueContext } from "./RedmatchGrammar";
import { ReferenceContext } from "./RedmatchGrammar";
import { VariableIdentifierContext } from "./RedmatchGrammar";


/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by `RedmatchGrammar`.
 *
 * @param <Result> The return type of the visit operation. Use `void` for
 * operations with no return type.
 */
export interface RedmatchGrammarVisitor<Result> extends ParseTreeVisitor<Result> {
	/**
	 * Visit a parse tree produced by `RedmatchGrammar.document`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitDocument?: (ctx: DocumentContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.fcRule`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitFcRule?: (ctx: FcRuleContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.fcBody`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitFcBody?: (ctx: FcBodyContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.repeatsClause`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitRepeatsClause?: (ctx: RepeatsClauseContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.condition`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitCondition?: (ctx: ConditionContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.resource`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitResource?: (ctx: ResourceContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.attribute`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitAttribute?: (ctx: AttributeContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.value`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitValue?: (ctx: ValueContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.reference`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitReference?: (ctx: ReferenceContext) => Result;

	/**
	 * Visit a parse tree produced by `RedmatchGrammar.variableIdentifier`.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	visitVariableIdentifier?: (ctx: VariableIdentifierContext) => Result;
}

