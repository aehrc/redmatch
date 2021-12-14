/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import au.csiro.redmatch.importer.RedcapCsvImporter;
import au.csiro.redmatch.importer.RedcapJsonImporter;
import au.csiro.redmatch.importer.SchemaImporter;
import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.ReplacementSuggestion;
import au.csiro.redmatch.model.LabeledField;
import au.csiro.redmatch.util.GraphUtils;
import au.csiro.redmatch.util.StringUtils;
import au.csiro.redmatch.validation.RedmatchGrammarValidator;
import au.csiro.redmatch.validation.ValidationResult;
import com.google.gson.Gson;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import au.csiro.redmatch.grammar.RedmatchGrammar;
import au.csiro.redmatch.grammar.RedmatchGrammar.AliasContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.AliasesContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.AttributeContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.AttributePathContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ConditionContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.DocumentContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.FcBodyContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.FcRuleContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.MappingsContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.MappingContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ReferenceContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.RepeatsClauseContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ResourceContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.SchemaContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ValueContext;
import au.csiro.redmatch.grammar.RedmatchGrammarBaseVisitor;
import au.csiro.redmatch.grammar.RedmatchLexer;
import org.springframework.beans.factory.annotation.Autowired;

import static au.csiro.redmatch.compiler.ErrorCodes.*;

/**
 * The compiler for the Redmatch rules language.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class RedmatchCompiler extends RedmatchGrammarBaseVisitor<GrammarObject> {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchCompiler.class);

  /**
   * Constants that indicate the source of a diagnostic message.
   */
  private static final String SRC_LEXER = "src_lexer";
  private static final String SRC_PARSER = "src_parser";
  private static final String SRC_COMPILER = "src_compiler";


  /**
   * Url for a FHIR resource. Used to identify "any" references.
   */
  private final static String RESOURCE_URL = "http://hl7.org/fhir/StructureDefinition/Resource";
  
  /**
   * Pattern to validate FHIR ids.
   */
  private final Pattern fhirIdPattern = Pattern.compile("[A-Za-z0-9\\-.]{1,64}");

  /**
   * Pattern used to generate valid FHIR ids.
   */
  private final Pattern partialFhirIdPattern = Pattern.compile("[A-Za-z0-9-.]+");
  
  /**
   * Pattern to validate REDCap form ids.
   */
  private final Pattern redcapIdPattern = Pattern.compile("[a-z][A-Za-z0-9_]*");
  
  /**
   * Pattern to identify Redmatch variables.
   */
  private final Pattern redmatchVariablePattern = Pattern.compile("[$][{][a-zA-Z0-9_]+[}]");
  
  /**
   * Stores the path information for the leaf node - used to validate each value assignment.
   */
  private PathInfo lastInfo = null;

  /**
   * A map to keep any aliases defined in the document.
   */
  private final Map<String, String> aliases = new HashMap<>();

  /**
   * The schema of the source data.
   */
  private au.csiro.redmatch.model.Schema schema;

  /**
   * Diagnostic messages produced by the compilation process.
   */
  private final List<Diagnostic> diagnostics = new ArrayList<>();

  /**
   * Validator for FHIR attribute expressions.
   */
  private final RedmatchGrammarValidator validator;

  /**
   * Reference to Gson, in case the schema is in JSON format.
   */
  private final Gson gson;

  /**
   * The base folder where the input files are contained. Can be null if the compiler is used in a server environment.
   */
  private File baseFolder;

  /**
   * Constructor.
   */
  @Autowired
  public RedmatchCompiler(RedmatchGrammarValidator validator, Gson gson) {
    this.validator = validator;
    this.gson = gson;
  }

  /**
   * Compiles a Redmatch document.
   *
   * @param baseFolder The base folder where the input files are contained.
   * @param document The Redmatch document.
   *
   * @return A Document object or null if there is an unrecoverable compilation problem.
   */
  public Document compile(File baseFolder, String document) {
    clear();

    this.baseFolder = baseFolder;
    if (baseFolder != null) {
      log.info("Running compilation from base folder: " + baseFolder.getAbsolutePath());
    } else {
      log.info("Running compilation with no base folder");
    }

    if (document == null || document.isEmpty()) {
      return new Document();
    }

    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(document));
    final RedmatchGrammar parser = new RedmatchGrammar(new CommonTokenStream(lexer));

    lexer.removeErrorListeners();
    lexer.addErrorListener(new DiagnosticErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException e) {
        addError(offendingSymbol != null ? offendingSymbol.toString() : "", line, charPositionInLine, msg, SRC_LEXER,
          CODE_LEXER.toString());
      }
    });

    parser.removeErrorListeners();
    parser.addErrorListener(new DiagnosticErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                              int charPositionInLine, String msg, RecognitionException e) {
        addError(offendingSymbol != null ? offendingSymbol.toString() : "", line, charPositionInLine, msg, SRC_PARSER,
          CODE_PARSER.toString());
      }
    });

    if (Thread.interrupted()) {
      log.debug("Interrupting compilation");
      return null;
    }
    final DocumentContext docCtx = parser.document();

    // We need to check if the EOF token was matched. If not, then there is a problem.
    final Token finalToken = lexer.getToken();
    if (finalToken.getType() != Token.EOF) {
      addError(finalToken.getText(), finalToken.getLine(), finalToken.getCharPositionInLine(),
        "Unexpected token '" + finalToken.getText() + "'.", SRC_COMPILER, CODE_PARSER.toString());
    }

    String tree = docCtx.toStringTree(parser);
    if (log.isDebugEnabled()) {
      printPrettyLispTree(tree);
    }

    try {
      if (Thread.interrupted()) {
        log.debug("Interrupting compilation");
        return null;
      }
      Document doc = (Document) docCtx.accept(this);
      doc.setDiagnostics(new ArrayList<>(this.diagnostics));

      // Validate the resulting FHIR graph if there are no errors
      if (this.diagnostics.stream().noneMatch(d -> d.getSeverity().equals(DiagnosticSeverity.Error))) {
        if (Thread.interrupted()) {
          log.debug("Interrupting compilation");
          return null;
        }
        GraphUtils.Results res = GraphUtils.buildGraph(doc);
        doc.getDiagnostics().addAll(res.getDiagnostics());
      }
      return doc;
    } catch (Throwable t) {
      log.error("There was an unexpected problem compiling the rules.", t);
      throw new CompilationException("There was an unexpected problem compiling the rules.", t);
    }
  }

  /**
   * Compiles a Redmatch document.
   *
   * @param document The Redmatch document.
   * 
   * @return A Document object or null if there is an unrecoverable compilation problem.
   */
  public synchronized Document compile(String document) {
    return this.compile(null, document);
  }
  
  /**
   * Entry point for visitor. This is the only method that should be called.
   */
  @Override
  public GrammarObject visitDocument(DocumentContext ctx) {
    final Document res = new Document();
    
    // If parsing produced errors then do not continue
    if (!diagnostics.isEmpty()) {
      res.setDiagnostics(this.diagnostics);
      return res;
    }

    // Load schema
    Schema sc = (Schema) visitSchemaInternal(ctx.schema());
    if (sc == null) {
      this.diagnostics.add(getDiagnosticFromContext(ctx, "Invalid schema definition", DiagnosticSeverity.Error,
        CODE_INVALID_SCHEMA.toString()));
      return res;
    } else if (!sc.isValid()) {
      this.diagnostics.add(getDiagnosticFromContext(ctx, "Schema could not be loaded from " + sc.getSchemaLocation(),
        DiagnosticSeverity.Error, CODE_UNABLE_TO_LOAD_SCHEMA.toString()));
      return res;
    }

    if (sc.getSchemaType().equals("REDCAP")) {
      au.csiro.redmatch.model.Schema s = loadRedcapSchema(sc.getSchema());
      if (s == null) {
        this.diagnostics.add(getDiagnosticFromContext(ctx, "Unknown REDCap schema type.", DiagnosticSeverity.Error,
          CODE_UNKNOWN_REDCAP_SCHEMA_TYPE.toString()));
        return res;
      }
      this.schema = s;
      res.setSchema(this.schema);
    } else {
      return res;
    }

    // Get server if present
    if (ctx.server() != null && ctx.server().STRING() != null) {
      res.setServer(removeEnds(ctx.server().STRING().getText()));
    }

    // Process aliases and store in a map
    AliasesContext aliases = ctx.aliases();
    if (aliases != null) {
      for (AliasContext alias : aliases.alias()) {
        String key = alias.ALIAS().getText();
        String val = removeEnds(alias.STRING().getText());
        log.debug("Found alias " + key + " with value " + val);
        this.aliases.put(key, val);
      }
    }

    // Process rules
    for (FcRuleContext rule : ctx.rules().fcRule()) {
      final Variables var = new Variables();
      GrammarObject go =  visitFcRuleInternal(rule, var);
      if (go instanceof Rule) {
        res.getRules().add((Rule) go);
      } else if (go instanceof RuleList) {
        RuleList rl = (RuleList) go;
        res.getRules().addAll(rl.getRules());
      } else {
        getDiagnosticFromContext(
          ctx,
          "Unexpected type " + go.getClass().getCanonicalName() + ". Expected Rule or RuleList.",
          DiagnosticSeverity.Error,
          CODE_COMPILER_ERROR.toString()
        );
      }
    }

    // Before we process mappings, we need to find the fields that need a mapping. This will let us generate compilation
    // errors if a required mapping is missing and also warnings if a mapping that is not needed is present.
    Set<String> fieldsThatNeedMapping = getFieldsThatNeedMapping(res.getRules());

    // Process mappings
    MappingsContext mappingsCtx = ctx.mappings();
    if (mappingsCtx != null) {
      for (MappingContext mappingCtx : mappingsCtx.mapping()) {
        String fieldId = mappingCtx.ID().getText();
        Field f = this.schema.getField(fieldId);
        if (f == null) {
          this.diagnostics.add(
            getDiagnosticFromContext(
              mappingCtx,
              "Mapped field " + fieldId + " does not exist.",
              DiagnosticSeverity.Error,
              CODE_MAPPED_FIELD_DOES_NOT_EXIST.toString(),
              fieldId
            )
          );
          continue;
        }

        // Validate description matches field
        if (mappingCtx.STRING() != null) {
          String fieldLabel = removeEnds(mappingCtx.STRING().getText());
          LabeledField labeledField = new LabeledField(f);
          if(!fieldLabel.equals(f.getLabel())) {
            this.diagnostics.add(
              getDiagnosticFromContext(
                mappingCtx,
                "Label of mapped field " + fieldId + " in rules does not match schema (label in rules: '" + fieldLabel
                  + "', label in schema: '" + f.getLabel() + "')",
                DiagnosticSeverity.Warning,
                CODE_MAPPED_FIELD_LABEL_MISMATCH.toString(),
                labeledField
              )
            );
          }
        }

        ConceptLiteralValue clv = processMapTarget(mappingCtx);
        Mapping m = new Mapping(fieldId, clv.getSystem(), clv.getCode(), clv.getDisplay());
        res.getMappings().put(fieldId, m);
        if (!fieldsThatNeedMapping.remove(fieldId)) {
          this.diagnostics.add(
            getDiagnosticFromContext(
              mappingCtx,
              "Mapping for field " + fieldId + " is not needed.",
              DiagnosticSeverity.Warning,
              CODE_MAPPING_NOT_NEEDED.toString()
            )
          );
        }
      }
    }

    for (String fieldId : fieldsThatNeedMapping) {
      LabeledField labeledField = new LabeledField(schema.getField(fieldId));
      if (mappingsCtx != null) {
        this.diagnostics.add(
          getDiagnosticFromContext(
            mappingsCtx,
            "Mapping for field " + fieldId + " is required but was not found.",
            DiagnosticSeverity.Error,
            CODE_MAPPING_MISSING.toString(),
            fieldId
          )
        );
      } else {
        this.diagnostics.add(
          getDiagnosticFromContext(
            ctx,
            "Mapping for field " + fieldId + " is required but was not found.",
            DiagnosticSeverity.Error,
            CODE_MAPPING_AND_SECTION_MISSING.toString(),
            labeledField
          )
        );
      }
    }

    return res;
  }

  /**
   * Returns a set of fields that require a mapping.
   *
   * @param rules The list of transformation rules.
   * @return The set of fields that require a mapping.
   */
  private Set<String> getFieldsThatNeedMapping(List<Rule> rules) {
    Set<String> fields = new HashSet<>();
    for (Rule rule : rules) {
      for (Resource res : rule.getResources()) {
        for (AttributeValue av : res.getResourceAttributeValues()) {
          Value val = av.getValue();
          if (val instanceof CodeSelectedValue || val instanceof ConceptSelectedValue) {
            FieldBasedValue fbv = (FieldBasedValue) val;
            String fieldId = fbv.getFieldId();
            Field f = schema.getField(fieldId);
            if (f == null) {
              continue;
            }
            for (Field opt : f.getOptions()) {
              fields.add(opt.getFieldId());
            }
          } else if (val instanceof  ConceptValue) {
            FieldBasedValue fbv = (FieldBasedValue) val;
            String fieldId = fbv.getFieldId();
            Field f = schema.getField(fieldId);
            if (f == null) {
              continue;
            }
            if (f.needsMapping(fbv)) {
              fields.add(fieldId);
            }
          }
        }
      }
    }

    return fields;
  }

  private au.csiro.redmatch.model.Schema loadRedcapSchema(File schemaFile) {
    String name = schemaFile.getName();
    SchemaImporter imp;
    if (name.endsWith(".json")) {
      imp = new RedcapJsonImporter(gson);
    } else if (name.endsWith(".csv")) {
      imp = new RedcapCsvImporter();
    } else {
      return null;
    }
    return imp.loadSchema(schemaFile);
  }

  private GrammarObject visitSchemaInternal(SchemaContext ctx) {
    String schemaType = ctx.SCHEMA_TYPE().getText();
    if("REDCAP".equals(schemaType)) {
      String schemaLocation = removeEnds(ctx.STRING().getText());
      return new Schema(this.baseFolder, schemaLocation, schemaType);
    } else {
      this.diagnostics.add(
        getDiagnosticFromContext(ctx, "Unsupported schema type: " + schemaType, DiagnosticSeverity.Error,
          CODE_UNSUPPORTED_SCHEMA.toString())
      );
      return null;
    }
  }

  private GrammarObject visitFcRuleInternal(FcRuleContext ctx, Variables var) {
    final Token start = ctx.getStart();
    final Token stop = ctx.getStop();
    
    int startRow = 0;
    int startCol = 0;
    int endRow;
    int endCol;
    
    if (start != null) {
      startRow = start.getLine();
      startCol = start.getCharPositionInLine();
    }
    
    if (stop != null) {
      endRow = stop.getLine();
      endCol = stop.getCharPositionInLine() + 
          (stop.getText() != null ? stop.getText().length() : 1);
    } else {
      endRow = startRow;
      endCol = startCol + 1;
    }

    if (ctx.repeatsClause() != null) {
      final RuleList res = new RuleList();
      
      final RepeatsClause rc = visitRepeatsClauseInternal(ctx.repeatsClause());
      for (int i = rc.getStart(); i <= rc.getEnd(); i++) {
        Variables newVar = new Variables(var);
        newVar.addVariable(rc.getVarName(), i);
        res.getRules().add(processRule(ctx, startRow, startCol, endRow, endCol, newVar));
      }
      return res;
    } else {
      return processRule(ctx, startRow, startCol, endRow, endCol, var);
    }
  }
  
  private Rule processRule(FcRuleContext ctx, int startRow, int startCol, int endRow, int endCol, Variables var) {
    final Rule res = new Rule(startRow, startCol, endRow, endCol);
    if (ctx.condition() != null) {
      final Condition c = (Condition) visitConditionInternal(ctx.condition(), var);
      res.setCondition(c);
    } else {
      this.diagnostics.add(
        getDiagnosticFromContext(ctx, "Expected a condition but it was null.", DiagnosticSeverity.Error,
          CODE_COMPILER_ERROR.toString())
      );
    }
    if (ctx.fcBody().size() > 0) {
      res.setBody(visitFcBodyInternal(ctx.fcBody(0), var));
    } else {
      this.diagnostics.add(
        getDiagnosticFromContext(ctx,"Expected at least one body but found none.", DiagnosticSeverity.Error,
          CODE_COMPILER_ERROR.toString())
      );
    }
    if (ctx.fcBody().size() > 1) {
      res.setElseBody(visitFcBodyInternal(ctx.fcBody(1), var));
    }
    return res;
  }

  private Body visitFcBodyInternal(FcBodyContext ctx, Variables var) {
    final Body b = new Body();
    
    for(ResourceContext rc :  ctx.resource()) {
      b.getResources().add(visitResourceInternal(rc, var));
    }
    
    for(FcRuleContext rc : ctx.fcRule()) {
      GrammarObject go = visitFcRuleInternal(rc, var);
      if (go instanceof Rule) {
        b.getRules().add((Rule) go);
      } else if (go instanceof RuleList) {
        b.getRules().addAll(((RuleList) go).getRules());
      }
    }
    
    return b;
  }

  private RepeatsClause visitRepeatsClauseInternal(RepeatsClauseContext ctx) {
    int start = Integer.parseInt(ctx.NUMBER(0).getText());
    int end = Integer.parseInt(ctx.NUMBER(1).getText());
    final String varName = ctx.ID().getText();
    return new RepeatsClause(start, end, varName);
  }

  private String removeEnds(String s) {
    if (s.length() < 2) {
      return s;
    }
    return s.substring(1, s.length() - 1);
  }
  
  private String processFhirOrRedcapId(TerminalNode tn, Token t, Variables var) {
    // Determine if the token has a Redmatch variable in it and if so replace it with its actual
    // value - this will look like #cmdt_zyg1 or #cmdt_zyg${x} 
    String s = t.getText();
    final Matcher m = redmatchVariablePattern.matcher(s);
    StringBuilder sb = new StringBuilder();
    int start = 0;
    while (m.find()) {
      sb.append(s, start, m.start());
      String g = m.group();
      String v = g.substring(2, g.length() - 1);
      int val = 0;
      try {
        val = var.getValue(v);
      } catch (UnknownVariableException e) {
        this.diagnostics.add(getDiagnosticFromTerminalNode(tn, e.getLocalizedMessage(), DiagnosticSeverity.Error,
          CODE_UNKNOWN_VARIABLE.toString()));
      }
      sb.append(val);
      start = m.end();
    }
    if (start < s.length()) {
      sb.append(s.substring(start));
    }
    
    return sb.toString();
  }
  
  private String processRedcapId(TerminalNode tn, Token t, Variables var) {
    String text = processFhirOrRedcapId(tn, t, var);
    if (!redcapIdPattern.matcher(text).matches()) {
      this.diagnostics.add(
        getDiagnosticFromTerminalNode(
          tn,
          "Invalid REDCap id '" + text + "': must match this regex: [a-z][A-Za-z0-9_]*",
          DiagnosticSeverity.Error,
          CODE_INVALID_REDCAP_ID.toString(),
          new ReplacementSuggestion(text, getClosestRedcapId(text))
        )
      );
    }

    if (!this.schema.hasField(text)) {
      this.diagnostics.add(
        getDiagnosticFromTerminalNode(tn,
          "Field " + text + " does not exist in REDCap schema.",
          DiagnosticSeverity.Error,
          CODE_UNKNOWN_REDCAP_FIELD.toString(),
          new ReplacementSuggestion(text, getClosestRedcapId(text))
        )
      );
    }
    return text;
  }
  
  private String processFhirId(TerminalNode tn, Token t, Variables var) {
    String text = processFhirOrRedcapId(tn, t, var);
    if (!fhirIdPattern.matcher(text).matches()) {
      this.diagnostics.add(
        getDiagnosticFromTerminalNode(
          tn,
          "Invalid FHIR id '" + text + "': must match this regex: [A-Za-z0-9\\-\\.]{1,64}",
          DiagnosticSeverity.Error,
          CODE_INVALID_FHIR_ID.toString(),
          new ReplacementSuggestion(text, fhiriseId(text))
        )
      );
    }
    return text;
  }

  private GrammarObject visitConditionInternal(ConditionContext ctx, Variables var) {
    ParseTree first = ctx.getChild(0);
    if (first instanceof TerminalNode) {
      TerminalNode tn = (TerminalNode) first;
      String text = tn.getSymbol().getText();
      if ("^".equals(text)) {
        final Condition c = (Condition) visitConditionInternal(ctx.condition(0), var);
        assert c != null;
        c.setNegated(true);
        return c;
      } else if ("TRUE".equals(text)) {
        return new ConditionExpression(true);
      } else if ("FALSE".equals(text)) {
        return new ConditionExpression(false);
      } else if ("NULL".equals(text)) {
        String id = processRedcapId(ctx.ID(), ctx.ID().getSymbol(), var);
        return new ConditionExpression(id, true);
      } else if ("NOTNULL".equals(text)) {
        String id = processRedcapId(ctx.ID(), ctx.ID().getSymbol(), var);
        return new ConditionExpression(id, false);
      } else if ("VALUE".equals(text)) {
        if (ctx.getChildCount() < 6) {
          this.diagnostics.add(
            getDiagnosticFromContext(
              ctx,
              "Expected at least 6 children but found " + ctx.getChildCount(),
              DiagnosticSeverity.Error,
              CODE_COMPILER_ERROR.toString()
            )
          );
          return null;
        }
        
        String id = processRedcapId(ctx.ID(), ctx.ID().getSymbol(), var);
        String ops = ctx.getChild(4).getText();
        ConditionExpression.ConditionExpressionOperator op = getOp(ops);
        if (op == null) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "Unexpected operator " + ops, DiagnosticSeverity.Error,
            CODE_COMPILER_ERROR.toString()));
        } else if (ctx.STRING() != null) {
          return new ConditionExpression(id, op, removeEnds(ctx.STRING().getText()));
        } else if (ctx.NUMBER() != null) {
          String num = ctx.NUMBER().getText();
          if (num.contains(".")) {
            return new ConditionExpression(id, op, Double.parseDouble(ctx.NUMBER().getText()));
          } else {
            return new ConditionExpression(id, op, Integer.parseInt(ctx.NUMBER().getText()));
          }
        }
      } else if ("(".equals(text)) {
        return visitConditionInternal(ctx.condition(0), var);
      } else {
        this.diagnostics.add(getDiagnosticFromContext(ctx,
            "Expected TRUE, FALSE, NULL, NOTNULL, VALUE or ( but found  " + text, DiagnosticSeverity.Error,
            CODE_COMPILER_ERROR.toString()));
      }
    } else {
      if (ctx.getChildCount() < 2) {
        this.diagnostics.add(getDiagnosticFromContext(ctx,
            "Expected at least two children but found " + ctx.getChildCount(), DiagnosticSeverity.Error,
            CODE_COMPILER_ERROR.toString()));
      }
      ParseTree second = ctx.getChild(1);
      if (second instanceof TerminalNode) {
        TerminalNode tn = (TerminalNode) second;
        String text = tn.getSymbol().getText();
        if ("&".equals(text)) {
          return new ConditionNode((Condition) visitConditionInternal(ctx.condition(0), var),
              ConditionNode.ConditionNodeOperator.AND,
              (Condition) visitConditionInternal(ctx.condition(1), var));
        } else if ("|".equals(text)) {
          return new ConditionNode((Condition) visitConditionInternal(ctx.condition(0), var),
              ConditionNode.ConditionNodeOperator.OR,
              (Condition) visitConditionInternal(ctx.condition(1), var));
        } else {
          this.diagnostics.add(
            getDiagnosticFromContext(ctx, "Expected & or | but found " + text, DiagnosticSeverity.Error,
              CODE_COMPILER_ERROR.toString())
          );
        }
      } else {
        this.diagnostics.add(
          getDiagnosticFromContext(ctx, "Expected a terminal node but found " + second, DiagnosticSeverity.Error,
            CODE_COMPILER_ERROR.toString())
        );
      }
    }
    return new ConditionExpression(false);
  }
  
  private ConditionExpression.ConditionExpressionOperator getOp(String ops) {
    if ("=".equals(ops)) {
      return ConditionExpression.ConditionExpressionOperator.EQ;
    } else if ("!=".equals(ops)) {
      return ConditionExpression.ConditionExpressionOperator.NEQ;
    } else if ("<".equals(ops)) {
      return ConditionExpression.ConditionExpressionOperator.LT;
    } else if (">".equals(ops)) {
      return ConditionExpression.ConditionExpressionOperator.GT;
    } else if ("<=".equals(ops)) {
      return ConditionExpression.ConditionExpressionOperator.LTE;
    } else if (">=".equals(ops)) {
      return ConditionExpression.ConditionExpressionOperator.GTE;
    } else {
      return null;
    }
  }

  private Resource visitResourceInternal(ResourceContext ctx, Variables var) {
    final Resource res = new Resource();
    if (ctx == null) {
      return res;
    }
    res.setResourceType(ctx.RESOURCE().getText());
    
    String resourceId = processFhirId(ctx.ID(), ctx.ID().getSymbol(), var);
    res.setResourceId(resourceId);
    
    for (int i = 0; i < ctx.attribute().size(); i++) {
      AttributeValue av = new AttributeValue();
      av.setAttributes(visitAttributeInternal(res.getResourceType(), ctx.attribute(i)));
      av.setValue(visitValueInternal(ctx.value(i), var));
      res.getResourceAttributeValues().add(av);
    }

    return res;
  }

  private Attribute visitAttributePathInternal(AttributePathContext ctx) {
    Attribute att = new Attribute();
    att.setName(ctx.PATH().getText());
    if (ctx.INDEX() != null) {
      att.setAttributeIndex(Integer.parseInt(ctx.INDEX().getText()));
    }
    return att;
  }

  private List<Attribute> visitAttributeInternal(String resourceType, AttributeContext ctx) {
    final List<Attribute> res = new ArrayList<>();
    String path = resourceType;
    
    for (AttributePathContext apCtx : ctx.attributePath()) {
      Attribute att = visitAttributePathInternal(apCtx);
      res.add(att);
      
      // Validate attribute
      path = path + "." + att.getName();
      log.debug("Validating path " + path);
      ValidationResult vr = validator.validateAttributePath(path);
      if (!vr.getResult()) {
        for (String msg : vr.getMessages()) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, msg, DiagnosticSeverity.Error,
            CODE_INVALID_FHIR_ATTRIBUTE_PATH.toString()));
        }
        break;
      } else {
        // TODO: check what happens with extension[0].valueReference = REF(ResearchStudy<rstud>)
        // Add test case for FHIR exporter with an extension
        PathInfo info = validator.getPathInfo(path);
        lastInfo = info;
        
        String max = info.getMax();
        if ("*".equals(max)) {
          att.setList(true);
        } else {
          int maxInt = Integer.parseInt(max);
          if (maxInt == 0) {
            this.diagnostics.add(getDiagnosticFromContext(ctx, "Unable to set attribute "
                + path + " with max cardinality of 0.", DiagnosticSeverity.Error,
              CODE_FHIR_ATTRIBUTE_NOT_ALLOWED.toString()));
            break;
          } else if (att.hasAttributeIndex() && att.getAttributeIndex() >= maxInt) {
            // e.g. myAttr[1] would be illegal if maxInt = 1
            this.diagnostics.add(getDiagnosticFromContext(ctx, "Attribute " + att +
              " is setting an invalid index (max = " + maxInt + ").", DiagnosticSeverity.Error,
              CODE_INVALID_FHIR_ATTRIBUTE_INDEX.toString()));
            break;
          }
        }
      }
    }
    
    return res;
  }

  private Value visitValueInternal(ValueContext ctx, Variables var) {
    // Check Redmatch expression is compatible with REDCap field type
    final String errorMsg = "%s cannot be assigned to attribute of type %s";
    if(lastInfo == null) {
      this.diagnostics.add(
        getDiagnosticFromContext(ctx, "No path information for leaf node", DiagnosticSeverity.Error,
          CODE_COMPILER_ERROR.toString())
      );
      return null;
    }

    final String type = lastInfo.getType();
    
    if (ctx.TRUE() != null) {
      if (!type.equals("boolean")) {
        this.diagnostics.add(
          getDiagnosticFromContext(ctx, String.format(errorMsg, "Boolean value", type), DiagnosticSeverity.Error,
            CODE_INCOMPATIBLE_TYPE.toString())
        );
      }
      return new BooleanValue(true);
    } else if (ctx.FALSE() != null) {
      return new BooleanValue(false);
    }  else if (ctx.VALUE() != null) {
      String fieldId = processRedcapId(ctx.ID(), ctx.ID().getSymbol(), var);

      // Validate REDCap field exists
      if (!this.schema.hasField(fieldId)) {
        this.diagnostics.add(
          getDiagnosticFromTerminalNode(
            ctx.ID(),
            "Field " + fieldId + " does not exist in REDCap.",
            DiagnosticSeverity.Error,
            CODE_UNKNOWN_REDCAP_FIELD.toString(),
            new ReplacementSuggestion(fieldId, getClosestRedcapId(fieldId))
          )
        );
      }

      FieldBasedValue val = null;

      // Extract the new optional string - YEAR, MONTH, DAY
      if (ctx.STRING() != null) {
        String str = removeEnds(ctx.STRING().getText());
        if (str.equalsIgnoreCase("YEAR")) {
           val = new FieldValue(fieldId, FieldValue.DatePrecision.YEAR);
        } else if (str.equalsIgnoreCase("MONTH")) {
          val = new FieldValue(fieldId, FieldValue.DatePrecision.MONTH);
        } else if (str.equalsIgnoreCase("DAY")) {
          val = new FieldValue(fieldId, FieldValue.DatePrecision.DAY);
        } else {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "Invalid value " + str
            + ". Valid values are YEAR, MONTH and DAY.", DiagnosticSeverity.Error,
            CODE_INVALID_DATE_PRECISION.toString()));
        }
      }
      if (val == null) {
        val = new FieldValue(fieldId);
      }
      return val;
    } else if (ctx.STRING() != null) {
      if (!(type.equals("string") || type.equals("markdown") || type.equals("id") 
          || type.equals("uri") || type.equals("oid") || type.equals("uuid") 
          || type.equals("canonical") || type.equals("url"))) {
        this.diagnostics.add(getDiagnosticFromContext(ctx, String.format(errorMsg, "String literal",
            type), DiagnosticSeverity.Error, CODE_INCOMPATIBLE_TYPE.toString()));
      }
      
      String str = removeEnds(ctx.STRING().getText());
      if (type.equals("id") && !fhirIdPattern.matcher(str).matches()) {
        this.diagnostics.add(
          getDiagnosticFromContext(
            ctx,
            "FHIR id " + str + " is invalid (it should match this regex: [A-Za-z0-9\\-\\.]{1,64})",
            DiagnosticSeverity.Error,
            CODE_INVALID_FHIR_ID.toString(),
            new ReplacementSuggestion(str, fhiriseId(str))
          )
        );
      } else if (type.equals("uri")) {
        try {
          log.debug("Checking URI: " + URI.create(str));
        } catch (IllegalArgumentException e) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "URI " + str + " is invalid: "
              + e.getLocalizedMessage(), DiagnosticSeverity.Error, CODE_INVALID_URI.toString()));
        }
      } else if (type.equals("oid")) {
        try {
          new Oid(str);
        } catch (GSSException e) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "OID " + str + " is invalid: "
              + e.getLocalizedMessage(), DiagnosticSeverity.Error, CODE_INVALID_OID.toString()));
        }
      } else if (type.equals("uuid")) {
        try {
          log.debug("Checking UUID: " + UUID.fromString(str));
        } catch (IllegalArgumentException e) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "UUID " + str + " is invalid: "
              + e.getLocalizedMessage(), DiagnosticSeverity.Error, CODE_INVALID_UUID.toString()));
        }
      } else if (type.equals("canonical")) {
        // Can have a version using |
        String uri = null;
        if (str.contains("|")) {
          String[] parts = str.split("[|]");
          if (parts.length != 2) {
            this.diagnostics.add(
              getDiagnosticFromContext(ctx, "Canonical " + str + " is invalid", DiagnosticSeverity.Error,
                CODE_INVALID_CANONICAL.toString())
            );
            uri = str; // to continue with validation
          } else {
            uri = parts[0];
          }
        }
        
        try {
          assert uri != null;
          log.debug("Checking URI: " + URI.create(uri));
        } catch (IllegalArgumentException e) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "Canonical " + str + " is invalid: "
              + e.getLocalizedMessage(), DiagnosticSeverity.Error, CODE_INVALID_CANONICAL.toString()));
        }
        
      } else if (type.equals("url")) {
        try {
          new URL(str);
        } catch (MalformedURLException e) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, "URL " + str + " is invalid: "
              + e.getLocalizedMessage(), DiagnosticSeverity.Error, CODE_INVALID_URL.toString()));
        }
      }
      
      return new StringValue(str);
    } else if (ctx.NUMBER() != null) {
      String num = ctx.NUMBER().getText();
      if (num.contains(".")) {
        return new DoubleValue(Double.parseDouble(num));
      } else {
        if (!type.equals("integer")) {
          this.diagnostics.add(getDiagnosticFromContext(ctx, String.format(errorMsg, "Integer literal",
              type), DiagnosticSeverity.Error, CODE_INCOMPATIBLE_TYPE.toString()));
        }
        return new IntegerValue(Integer.parseInt(num));
      }
    } else if (ctx.reference() != null) {
      // Subclasses of DomainResource
      // TODO: this checks the type is not primitive but complex types can still slip through
      if (!type.isEmpty() && !Character.isUpperCase(type.charAt(0))) {
        this.diagnostics.add(
          getDiagnosticFromContext(ctx, String.format(errorMsg, "Reference", type), DiagnosticSeverity.Error,
            CODE_INCOMPATIBLE_TYPE.toString())
        );
      }
      return visitReferenceInternal(ctx.reference(), var);
    } else if (ctx.CONCEPT_LITERAL() != null) {
      if (!type.equals("Coding") && !type.equals("CodeableConcept")) {
        this.diagnostics.add(
          getDiagnosticFromContext(ctx, String.format(errorMsg, "Concept literal", type), DiagnosticSeverity.Error,
            CODE_INCOMPATIBLE_TYPE.toString())
        );
      }

      String system;
      String code;
      String display = null;
      List<TerminalNode> parts = ctx.CL_PART();
      if (ctx.CL_ALIAS() != null) {
        system =  resolveAlias(ctx.CL_ALIAS(), ctx.CL_ALIAS().getText());
        if (!parts.isEmpty()) {
          code = parts.get(0).getText();
        } else {
          getDiagnosticFromContext(ctx, "Expected at least one CL_PART", DiagnosticSeverity.Error,
            CODE_COMPILER_ERROR.toString());
          code = "";
        }
      } else  {
        if (parts.size() >= 2) {
          system = parts.get(0).getText();
          code = parts.get(1).getText();
        } else {
          getDiagnosticFromContext(ctx, "Expected at least two CL_PARTs", DiagnosticSeverity.Error,
            CODE_COMPILER_ERROR.toString());
          system = "";
          code = "";
        }
      }

      if (ctx.STRING() != null) {
        display = removeEnds(ctx.STRING().getText());
      }

      if (display != null) {
        return new ConceptLiteralValue(system, code, display);
      } else {
        return new ConceptLiteralValue(system, code);
      }
    } else if (ctx.CONCEPT() != null || ctx.CONCEPT_SELECTED() != null || ctx.CODE_SELECTED() != null) {
      String fieldId = processRedcapId(ctx.ID(), ctx.ID().getSymbol(), var);
      FieldBasedValue val;

      if (ctx.CONCEPT() != null) {
        val = new ConceptValue(fieldId);
      } else if (ctx.CONCEPT_SELECTED() != null) {
        val = new ConceptSelectedValue(fieldId);
      } else if (ctx.CODE_SELECTED() != null) {
        val = new CodeSelectedValue(fieldId);
      } else {
        getDiagnosticFromContext(ctx, "Invalid expression", DiagnosticSeverity.Error, CODE_COMPILER_ERROR.toString());
        return null;
      }
      validateField(fieldId, val, ctx);
      return val;
    } else if (ctx.CODE() != null) {
      return new CodeLiteralValue(ctx.C_ID().getSymbol().getText());
    } else {
      this.diagnostics.add(getDiagnosticFromContext(ctx, "Unexpected value context: " + ctx, DiagnosticSeverity.Error,
        CODE_COMPILER_ERROR.toString()));
      return null;
    }
  }

  private void validateField(String fieldId, FieldBasedValue val, ParserRuleContext ctx) {
    List<String> msgs = new ArrayList<>();
    Field field = this.schema.getField(fieldId);
    if (field != null) {
      if (!field.isCompatibleWith(val, msgs)) {
        msgs.forEach(m -> this.diagnostics.add(getDiagnosticFromContext(ctx, m, DiagnosticSeverity.Error,
          CODE_INCOMPATIBLE_EXPRESSION.toString())));
      }
    }
  }

  private ConceptLiteralValue processMapTarget(MappingContext ctx) {
    String system;
    String code;
    String display = null;
    List<TerminalNode> parts = ctx.CL_PART();
    if (ctx.CL_ALIAS() != null) {
      system = resolveAlias(ctx.CL_ALIAS(), ctx.CL_ALIAS().getText());
      if (!parts.isEmpty()) {
        code = parts.get(0).getText();
      } else {
        getDiagnosticFromContext(ctx, "Expected at least one CL_PART", DiagnosticSeverity.Error,
          CODE_COMPILER_ERROR.toString());
        code = "";
      }
    } else  {
      if (parts.size() >= 2) {
        system = parts.get(0).getText();
        code = parts.get(1).getText();
      } else {
        getDiagnosticFromContext(ctx, "Expected at least two CL_PARTs", DiagnosticSeverity.Error,
          CODE_COMPILER_ERROR.toString());
        system = "";
        code = "";
      }
    }

    if (ctx.CL_STRING() != null) {
      display = removeEnds(ctx.CL_STRING().getText());
    }

    if (display != null) {
      return new ConceptLiteralValue(system, code, display);
    } else {
      return new ConceptLiteralValue(system, code);
    }
  }

  private String resolveAlias(TerminalNode tn, String s) {
    if (s.startsWith("$")) {
      String replacement = aliases.get(s);
      if (replacement != null) {
        return replacement;
      } else {
        this.diagnostics.add(
          getDiagnosticFromTerminalNode(
            tn,
            "Invalid alias found: " + s,
            DiagnosticSeverity.Error,
            CODE_INVALID_ALIAS.toString(),
            new ReplacementSuggestion(s, StringUtils.getClosest(s, aliases.keySet()))
          )
        );
        return s;
      }
    } else {
      return s;
    }
  }
  
  /*
   * reference
   *     : REF OPEN RESOURCE LT FHIR_OR_REDCAP_ID GT CLOSE
   *     ;
   *   
   * Example: REF(Patient<p>)
   */
  private Value visitReferenceInternal(ReferenceContext ctx, Variables var) {
    final String resType = ctx.RESOURCE().getText();
    
    // Validate reference based on target profiles
    final List<String> tgtProfiles = lastInfo.getTargetProfiles();
    
    // Special cases: no target profiles or target profile is Resource
    if (!tgtProfiles.isEmpty() 
        && !(tgtProfiles.size() == 1 && tgtProfiles.get(0).equals(RESOURCE_URL))) {
      // Otherwise we need to make sure that at least one applies
      boolean foundCompatible = false;
      for (String targetProfile : lastInfo.getTargetProfiles()) {
        if (targetProfile.endsWith(resType)) {
          foundCompatible = true;
          break;
        }
      }
      
      if (!foundCompatible) {
        this.diagnostics.add(getDiagnosticFromContext(ctx, "Attribute " + lastInfo.getPath()
            + " is of type reference but the resource type " + resType + " is incompatible. Valid "
            + "values are: " + String.join(",", lastInfo.getTargetProfiles()), DiagnosticSeverity.Error,
            CODE_INVALID_REFERENCE_TYPE.toString()));
      }
    }
    
    final ReferenceValue res = new ReferenceValue();
    res.setResourceType(resType);
    String id = processFhirId(ctx.ID(), ctx.ID().getSymbol(), var);
    res.setResourceId(id);
    
    return res;
  }

  private void addError(String token, int line, int charPositionInLine, String msg, String source, String code) {
    this.diagnostics.add(
      new Diagnostic(
        toRange(line, charPositionInLine, token),
        msg,
        DiagnosticSeverity.Error,
        source,
        code
      )
    );
  }

  private Range toRange(int line, int charPositionInLine, String token) {
    // Account for VSCode's 1-based indexing
    line = line - 1;

    Position start = new Position(line, charPositionInLine);
    Position end;
    if (token == null || token.isEmpty()) {
      end = new Position(line, charPositionInLine);
    } else {
      String[] lines = token.split("\\R");
      if (lines.length == 1) {
        end = new Position(line, charPositionInLine + token.length());
      } else {
        int endLine = line + lines.length - 1;
        int endPos = lines[lines.length - 1].length();
        end = new Position(endLine, endPos);
      }
    }
    return new Range(start, end);
  }

  private Diagnostic getDiagnosticFromTerminalNode(TerminalNode tn, String  msg, DiagnosticSeverity severity,
                                                   String code) {
    return getDiagnosticFromTerminalNode(tn, msg, severity, code, null);
  }

  private Diagnostic getDiagnosticFromTerminalNode(TerminalNode tn, String  msg, DiagnosticSeverity severity,
                                                   String code, Object data) {
    final Token token = tn.getSymbol();
    int startRow = token.getLine();
    int startCol = token.getCharPositionInLine();
    int endRow = token.getLine();
    int endCol = token.getCharPositionInLine() + token.getText().length();

    Diagnostic diagnostic =  new Diagnostic(
      // Account for VSCode's 1-based indexing
      new Range(new Position(startRow - 1, startCol), new Position(endRow - 1, endCol)),
      msg,
      severity,
      SRC_COMPILER,
      code
    );
    if (data != null) {
      diagnostic.setData(data);
    }
    return diagnostic;
  }

  private Diagnostic getDiagnosticFromContext(ParserRuleContext ctx, String  msg, DiagnosticSeverity severity,
                                              String code) {
    return getDiagnosticFromContext(ctx, msg, severity, code, null);
  }

  private Diagnostic getDiagnosticFromContext(ParserRuleContext ctx, String  msg, DiagnosticSeverity severity,
                                              String code, Object data) {
    final Token start = ctx.getStart();
    final Token stop = ctx.getStop();
    if (start == null || stop == null) {
      Diagnostic diagnostic = new Diagnostic(
        // Account for VSCode's 1-based indexing
        new Range(new Position(1, 1), new Position(1, 1)),
        msg,
        severity,
        SRC_COMPILER,
        code
      );
      if (data != null) {
        diagnostic.setData(data);
      }
      return diagnostic;
    }
    
    int startRow = start.getLine();
    int startCol = start.getCharPositionInLine();
    int endRow = stop.getLine();
    int endCol = stop.getCharPositionInLine() + (stop.getText() != null ? stop.getText().length() : 1);

    Diagnostic diagnostic = new Diagnostic(
      // Account for VSCode's 1-based indexing
      new Range(new Position(startRow - 1, startCol), new Position(endRow - 1, endCol)),
      msg,
      severity,
      SRC_COMPILER,
      code
    );
    if (data != null) {
      diagnostic.setData(data);
    }
    return diagnostic;
  }

  private void clear() {
    lastInfo = null;
    aliases.clear();
    schema = null;
    diagnostics.clear();
    baseFolder = null;
  }

  /**
   * Returns the closest Redcap id in the schema.
   *
   * @param id The actual id that is either invalid or does not exist.
   * @return The closest id in the schema.
   */
  private String getClosestRedcapId(String id) {
    List<String> candidates = schema.getFields().stream().map(Field::getFieldId).collect(Collectors.toList());
    return StringUtils.getClosest(id, candidates);
  }

  /**
   * Transforms an invalid FHIR id into a valid one.
   *
   * @param id The invalid FHIR id.
   * @return A valid FHIR id.
   */
  private String fhiriseId(String id) {
    if (id.isEmpty()) {
      return "id";
    }
    StringBuilder sb = new StringBuilder();
    Matcher matcher = partialFhirIdPattern.matcher(id);
    while (matcher.find()) {
      sb.append(matcher.group());
    }
    String res = sb.toString();
    if (res.length() > 64) {
      return res.substring(0, 64);
    } else {
      return res;
    }
  }
  
  private static void printPrettyLispTree(String tree) {
    int indentation = 1;
    for (char c : tree.toCharArray()) {
      if (c == '(') {
        if (indentation > 1) {
          System.out.println();
        }
        for (int i = 0; i < indentation; i++) {
          System.out.print("  ");
        }
        indentation++;
      }
      else if (c == ')') {
        indentation--;
      }
      System.out.print(c);
    }
    System.out.println();
  }
}
