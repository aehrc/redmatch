// Generated from src/grammar/au/csiro/redmatch/grammar/RedmatchGrammar.g4 by ANTLR 4.7.3-SNAPSHOT


import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";

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
 * This interface defines a complete listener for a parse tree produced by
 * `RedmatchGrammar`.
 */
export interface RedmatchGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by `RedmatchGrammar.document`.
	 * @param ctx the parse tree
	 */
	enterDocument?: (ctx: DocumentContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.document`.
	 * @param ctx the parse tree
	 */
	exitDocument?: (ctx: DocumentContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.fcRule`.
	 * @param ctx the parse tree
	 */
	enterFcRule?: (ctx: FcRuleContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.fcRule`.
	 * @param ctx the parse tree
	 */
	exitFcRule?: (ctx: FcRuleContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.fcBody`.
	 * @param ctx the parse tree
	 */
	enterFcBody?: (ctx: FcBodyContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.fcBody`.
	 * @param ctx the parse tree
	 */
	exitFcBody?: (ctx: FcBodyContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.repeatsClause`.
	 * @param ctx the parse tree
	 */
	enterRepeatsClause?: (ctx: RepeatsClauseContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.repeatsClause`.
	 * @param ctx the parse tree
	 */
	exitRepeatsClause?: (ctx: RepeatsClauseContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.condition`.
	 * @param ctx the parse tree
	 */
	enterCondition?: (ctx: ConditionContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.condition`.
	 * @param ctx the parse tree
	 */
	exitCondition?: (ctx: ConditionContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.resource`.
	 * @param ctx the parse tree
	 */
	enterResource?: (ctx: ResourceContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.resource`.
	 * @param ctx the parse tree
	 */
	exitResource?: (ctx: ResourceContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.attribute`.
	 * @param ctx the parse tree
	 */
	enterAttribute?: (ctx: AttributeContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.attribute`.
	 * @param ctx the parse tree
	 */
	exitAttribute?: (ctx: AttributeContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.value`.
	 * @param ctx the parse tree
	 */
	enterValue?: (ctx: ValueContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.value`.
	 * @param ctx the parse tree
	 */
	exitValue?: (ctx: ValueContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.reference`.
	 * @param ctx the parse tree
	 */
	enterReference?: (ctx: ReferenceContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.reference`.
	 * @param ctx the parse tree
	 */
	exitReference?: (ctx: ReferenceContext) => void;

	/**
	 * Enter a parse tree produced by `RedmatchGrammar.variableIdentifier`.
	 * @param ctx the parse tree
	 */
	enterVariableIdentifier?: (ctx: VariableIdentifierContext) => void;
	/**
	 * Exit a parse tree produced by `RedmatchGrammar.variableIdentifier`.
	 * @param ctx the parse tree
	 */
	exitVariableIdentifier?: (ctx: VariableIdentifierContext) => void;
}

