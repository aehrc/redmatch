/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use
 * is subject to license terms and conditions.
 */

package au.csiro.redmatch.compiler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.OidType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.UrlType;
import org.hl7.fhir.r4.model.UuidType;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.csiro.redmatch.exceptions.RedmatchException;
import au.csiro.redmatch.grammar.RedmatchGrammar;
import au.csiro.redmatch.grammar.RedmatchGrammar.AttributeContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ConditionContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.DocumentContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.FcBodyContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.FcRuleContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ReferenceContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.RepeatsClauseContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ResourceContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.ValueContext;
import au.csiro.redmatch.grammar.RedmatchGrammar.VariableIdentifierContext;
import au.csiro.redmatch.importer.CompilerException;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.AnnotationType;
import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Field.FieldType;
import au.csiro.redmatch.model.grammar.GrammarObject;
import au.csiro.redmatch.model.grammar.redmatch.Attribute;
import au.csiro.redmatch.model.grammar.redmatch.AttributeValue;
import au.csiro.redmatch.model.grammar.redmatch.Body;
import au.csiro.redmatch.model.grammar.redmatch.BooleanValue;
import au.csiro.redmatch.model.grammar.redmatch.CodeLiteralValue;
import au.csiro.redmatch.model.grammar.redmatch.CodeSelectedValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptLiteralValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptSelectedValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptValue;
import au.csiro.redmatch.model.grammar.redmatch.Condition;
import au.csiro.redmatch.model.grammar.redmatch.ConditionExpression;
import au.csiro.redmatch.model.grammar.redmatch.ConditionNode;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.model.grammar.redmatch.DoubleValue;
import au.csiro.redmatch.model.grammar.redmatch.FieldBasedValue;
import au.csiro.redmatch.model.grammar.redmatch.FieldValue;
import au.csiro.redmatch.model.grammar.redmatch.IntegerValue;
import au.csiro.redmatch.model.grammar.redmatch.ReferenceValue;
import au.csiro.redmatch.model.grammar.redmatch.RepeatsClause;
import au.csiro.redmatch.model.grammar.redmatch.Resource;
import au.csiro.redmatch.model.grammar.redmatch.Rule;
import au.csiro.redmatch.model.grammar.redmatch.RuleList;
import au.csiro.redmatch.model.grammar.redmatch.StringValue;
import au.csiro.redmatch.model.grammar.redmatch.Value;
import au.csiro.redmatch.model.grammar.redmatch.VariableIdentifier;
import au.csiro.redmatch.model.grammar.redmatch.Variables;
import au.csiro.redmatch.model.grammar.redmatch.ConditionExpression.ConditionExpressionOperator;
import au.csiro.redmatch.model.grammar.redmatch.ConditionNode.ConditionNodeOperator;
import au.csiro.redmatch.validation.RedmatchGrammarValidator;
import au.csiro.redmatch.validation.PathInfo;
import au.csiro.redmatch.validation.ValidationResult;
import au.csiro.redmatch.grammar.RedmatchGrammarBaseVisitor;
import au.csiro.redmatch.grammar.RedmatchLexer;

/**
 * The compiler for the Redmatch rules language.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component
public class RedmatchCompiler extends RedmatchGrammarBaseVisitor<GrammarObject> {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchCompiler.class);
  
  /**
   * Url for a FHIR resource. Used to identify "any" references.
   */
  private final static String RESOURCE_URL = "http://hl7.org/fhir/StructureDefinition/Resource";
  
  /**
   * The messages from the compiler in the previous compilation attempt.
   */
  private final List<Annotation> errorMessages = new ArrayList<>();
  
  /**
   * A component used to validate FHIR paths.
   */
  @Autowired
  private RedmatchGrammarValidator validator;
  
  /**
   * The REDCap metadata. Needed here so it is available in the 
   * {@link #visitDocument(DocumentContext)} method.
   */
  private Metadata metadata = null;
  
  /**
   * Pattern to validate FHIR ids.
   */
  private final Pattern idPattern = Pattern.compile("[A-Za-z0-9\\-\\.]{1,64}");
  
  
  /**
   * Compile rules.
   * 
   * @param rulesDocument A rules document.
   * @param metadata The REDCap metadata.
   * 
   * @return A Document object or null if there is an unrecoverable compilation problem.
   */
  public Document compile(String rulesDocument, Metadata metadata) {
    // Clear errors from previous compilations
    errorMessages.clear();
    
    this.metadata = metadata;
    
    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(rulesDocument));
    final RedmatchGrammar parser = new RedmatchGrammar(new BufferedTokenStream(lexer));
    parser.setErrorHandler(new DefaultErrorStrategy() {
      @Override
      public void reportInputMismatch(Parser recognizer, InputMismatchException e)
          throws RecognitionException {
        final Token token = recognizer.getCurrentToken();
        final int currentTokenType = token.getType();
        final String msg = "Mismatched input " + getTokenErrorDisplay(e.getOffendingToken())
          + " expecting " + e.getExpectedTokens().toString(recognizer.getVocabulary()) + " found "
          + (currentTokenType >= 0 ? recognizer.getVocabulary().getDisplayName(currentTokenType)
                : "end of input");
        
        
        addError(errorMessages, token.getText(), token.getLine(), 
            token.getCharPositionInLine(), msg);
        //recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
      }
    });

    lexer.removeErrorListeners();
    lexer.addErrorListener(new DiagnosticErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        addError(errorMessages, offendingSymbol != null ? offendingSymbol.toString() : "", 
            line, charPositionInLine, msg);
      }
    });

    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        addError(errorMessages, offendingSymbol != null ? offendingSymbol.toString() : "", 
            line, charPositionInLine, msg);
      }
    });
    
    final DocumentContext docCtx = parser.document();
    String tree = docCtx.toStringTree(parser);
    if (log.isDebugEnabled()) {
      printPrettyLispTree(tree);
    }
    
    try {
      GrammarObject res = docCtx.accept(this);
      final Document doc = (Document) res;
      return doc;
    } catch (Throwable t) {
      log.error("There was an unexpected problem compiling the rules.", t);
      throw new RedmatchException("There was an unexpected problem compiling the rules.", t);
    }
  }
  
  /**
   * Entry point for visitor. This is the only method that should be called.
   */
  @Override
  public GrammarObject visitDocument(DocumentContext ctx) {
    final Document res = new Document();
    
    // If parsing produced errors then do not continue
    if (hasErrors()) {
      return res;
    }
    
    for (FcRuleContext rule : ctx.fcRule()) {
      final Variables var = new Variables();
      GrammarObject go =  visitFcRuleInternal(rule, var, this.metadata);
      if (go instanceof Rule) {
        res.getRules().add((Rule) go);
      } else if (go instanceof RuleList) {
        RuleList rl = (RuleList) go;
        res.getRules().addAll(rl.getRules());
      } else {
        throw new RuntimeException("Unexpected type " + go.getClass().getCanonicalName() 
            + ". Expected Rule or RuleList. This should never happen!");
      }
      
    }
    return res;
  }
  
  /**
   * Get compilation messages from previous compilation attempt.
   * 
   * @return
   */
  public List<Annotation> getErrorMessages() {
    return errorMessages;
  }
  
  
  /**
   * Returns the validator.
   * 
   * @return
   */
  public RedmatchGrammarValidator getValidator() {
    return validator;
  }
  
  /**
   * Sets the validator.
   * 
   * @param validator
   */
  public void setValidator(RedmatchGrammarValidator validator) {
    this.validator = validator;
  }

  /**
   * This method validates a {@link Resource} doing the following:
   * 
   * <ol>
   *   <li>Checks that the path is valid, e.g., Patient &lt;p&gt; ->identifier.type is valid 
   *   because it is a valid path in the FHIR Patient resource.</li>
   *   <li>Checks that the Redmtach expression is compatible with the REDCap field type, e.g., 
   *   CONCEPT_SELECTED(phenotype___1) is valid because <i>phenotype___1</i> is a CHECKBOX_ENTRY 
   *   type field in REDCap. </li>
   *   <li>Checks that the type of the leaf attribute is compatible with the Redmatch expression
   *   used (see table below), e.g., Observation&lt;obs&gt; -> status = CODE_LITERAL(..) is valid 
   *   because <i>status</i> is of type <i>code</i>.</li>
   *   <li>Validates string literals depending on the FHIR type, e.g., if the type if URI then it
   *   valdiates the literal is a valid URI.</li>
   *   <li>For references, checks that the type of resource referenced in the rules matches the
   *   target profile, e.g., Condition<c> -> subject = REF(Patient<p>) is valid because the target
   *   profiles for Condition.subject are Patient and Group.</li>
   *   
   * </ol>
   * 
   * <table border="1">
   *    <tr>
   *     <td>Redmatch Expression</td>
   *     <td>Redmatch Expression Java Type</td>
   *     <td>Compatible FHIR attribute types</td>
   *   </tr>
   *   <tr>
   *     <td>TRUE, FALSE</td>
   *     <td>{@link BooleanValue}</td>
   *     <td>{@link BooleanType}</td>
   *   </tr>
   *   <tr>
   *     <td>CODE_LITERAL</td>
   *     <td>{@link CodeLiteralValue}</td>
   *     <td>{@link CodeType}, {@link Enumeration}</td>
   *   </tr>
   *   <tr>
   *     <td>CONCEPT_LITERAL</td>
   *     <td>{@link ConceptLiteralValue}</td>
   *     <td>{@link Coding}, {@link CodeableConcept}</td>
   *   </tr>
   *   <tr>
   *     <td>NUMBER</td>
   *     <td>{@link DoubleValue}</td>
   *     <td>{@link DecimalType}</td>
   *   </tr>
   *   <tr>
   *     <td>NUMBER</td>
   *     <td>{@link IntegerValue}</td>
   *     <td>{@link IntegerType}</td>
   *   </tr>
   *   <tr>
   *     <td>REF</td>
   *     <td>{@link ReferenceValue}</td>
   *     <td>Subclasses of {@link DomainResource}</td>
   *   </tr>
   *   <tr>
   *     <td>STRING</td>
   *     <td>{@link StringValue}</td>
   *     <td>{@link StringType}, {@link MarkdownType}, {@link IdType}, {@link UriType}, 
   *     {@link OidType}, {@link UuidType}, {@link CanonicalType}, {@link UrlType}</td>
   *   </tr>
   *   <tr>
   *     <td>CODE_SELECTED</td>
   *     <td>{@link CodeSelectedValue}</td>
   *     <td>{@link CodeType}, {@link Enumeration}</td>
   *   </tr>
   *   <tr>
   *     <td>CONCEPT_SELECTED</td>
   *     <td>{@link ConceptSelectedValue}</td>
   *     <td>{@link Coding}, {@link CodeableConcept}</td>
   *   </tr>
   *   <tr>
   *     <td>CONCEPT</td>
   *     <td>{@link ConceptValue}</td>
   *     <td>{@link Coding}, {@link CodeableConcept}</td>
   *   </tr>
   *   <tr>
   *     <td>VALUE</td>
   *     <td>{@link FieldValue}</td>
   *     <td>All FHIR primitive types. See <a 
   *     href="https://www.hl7.org/fhir/datatypes.html#primitive">here</a>.</td>
   *   </tr>
   * </table>
   * 
   * @param ctx The resource context.
   * @param r The resource.
   * @param meta The REDCap metadata.
   */
  private void validateResource(ResourceContext ctx, Resource r, Metadata meta) {
    String resourceType = r.getResourceType();
    
    // Iterate over all AttributeValues
    for (AttributeValue av : r.getResourceAttributeValues()) {
      
      // 1. Build path and validate
      PathInfo lastInfo = null; // Stores the path information for the leaf node
      boolean valid = true; // Indicates if this path is valid
      String path = resourceType;
      for (Attribute att : av.getAttributes()) {
        path = path + "." + att.getName();
        
        log.debug("Validating path " + path);
        ValidationResult vr = validator.validateAttributePath(path);
        if (!vr.getResult()) {
          for (String msg : vr.getMessages()) {
            errorMessages.add(getAnnotationFromContext(ctx, msg));
          }
          valid = false;
          break;
        } else {
          PathInfo info = validator.getPathInfo(path);
          lastInfo = info;
          
          String max = info.getMax();
          if ("*".equals(max)) {
            att.setList(true);
          } else {
            int maxInt = Integer.parseInt(max);
            if (maxInt == 0) {
              errorMessages.add(getAnnotationFromContext(ctx, "Unable to set attribute " 
                  + path + " with max cardinality of 0."));
              valid = false;
              break;
            } else if (att.hasAttributeIndex() && att.getAttributeIndex().intValue() >= maxInt) {
              // e.g. myAttr[1] would be illegal if maxInt = 1
              errorMessages.add(getAnnotationFromContext(ctx, "Attribute " 
                  + att.toString() + " is setting an invalid index (max = " + maxInt + ")."));
              valid = false;
              break;
            }
          }
        }
      }
      
      // No point in checking the rest if the path is not valid
      if (!valid) {
        continue;
      }
      
      // 2. Check the type of field is compatible with the expression used in the rule 
      final Value val = av.getValue();
      if (val instanceof FieldBasedValue) {
        final String fieldId = ((FieldBasedValue) val).getFieldId();
        
        final Field f = metadata.getField(fieldId);
        if (f == null) {
          errorMessages.add(getAnnotationFromContext(ctx, "Field " + fieldId 
              + " does not exist in REDCap."));
          continue;
        }
        
        final FieldType ft = f.getFieldType();
        
        // CONCEPT can only apply to a field of type TEXT, YESNO, DROPDOWN, RADIO, CHECKBOX, 
        // CHECKBOX_OPTION or TRUEFALSE
        if (val instanceof ConceptValue && !(ft.equals(FieldType.TEXT) 
            || ft.equals(FieldType.YESNO) || ft.equals(FieldType.DROPDOWN) 
            || ft.equals(FieldType.RADIO) || ft.equals(FieldType.DROPDOW_OR_RADIO_OPTION) 
            || ft.equals(FieldType.CHECKBOX) || ft.equals(FieldType.CHECKBOX_OPTION) 
            || ft.equals(FieldType.TRUEFALSE))) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              "The expression CONCEPT can only be used on fields of type TEXT, YESNO, DROPDOWN, "
              + "RADIO, DROPDOW_OR_RADIO_OPTION, CHECKBOX, CHECKBOX_OPTION or TRUEFALSE but field " 
              + fieldId + " is of type " + f.getFieldType()));
        }
        
        // CONCEPT_SELECTED can only apply to fields of type DROPDOW and RADIO
        if (val instanceof ConceptSelectedValue && !(ft.equals(FieldType.DROPDOWN) 
            || ft.equals(FieldType.RADIO))) {
          errorMessages.add(getAnnotationFromContext(ctx, "The expression CONCEPT_SELECTED "
              + "can only be used on fields of type DROPDOWN and RADIO but field " + fieldId 
              + " is of type " + f.getFieldType()));
        }
        
        // CODE_SELECTED can only apply to fields of type DROPDOWN and RADIO
        if (val instanceof CodeSelectedValue && !(ft.equals(FieldType.DROPDOWN) 
            || ft.equals(FieldType.RADIO))) {
          errorMessages.add(getAnnotationFromContext(ctx, "The expression CODE_SELECTED can "
              + "only be used on fields of type DROPDOWN and RADIO but field " + fieldId 
              + " is of type " + f.getFieldType()));
        }
        
        // VALUE can be used on any field
      }
      
      // 3. Check Redmatch expression is compatible with REDCap field type
      final String errorMsg = "%s cannot be assigned to attribute of type %s";
      final String type = lastInfo.getType();
      if (val instanceof BooleanValue) {
        if (!type.equals("boolean")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Boolean value", type)));
        }
      } else if (val instanceof CodeLiteralValue) {
        if (!type.equals("code")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Code literal", type)));
        }
      } else if (val instanceof ConceptLiteralValue) {
        if (!type.equals("Coding") && !type.equals("CodeableConcept")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Concept literal", type)));
        }
      } else if (val instanceof DoubleValue) {
        if (!type.equals("decimal")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Double literal", type)));
        }
      } else if (val instanceof IntegerValue) {
        if (!type.equals("integer")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Integer literal", type)));
        }
      } else if (val instanceof ReferenceValue) {
        // Subclasses of DomainResource
        // TODO: this checks the type is not primitive but complex types can still slip through
        if (!Character.isUpperCase(type.charAt(0))) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Reference", type)));
        }
      } else if (val instanceof StringValue) {
        if (!(type.equals("string") || type.equals("markdown") || type.equals("id") 
            || type.equals("uri") || type.equals("oid") || type.equals("uuid") 
            || type.equals("canonical") || type.equals("url"))) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "String literal", type)));
        }
        
        // 4. Validate string literals based on FHIR type
        StringValue sv = (StringValue) val;
        String s = removeEnds(sv.getStringValue());
        
        if (type.equals("id") && !idPattern.matcher(s).matches()) {
          errorMessages.add(getAnnotationFromContext(ctx, "FHIR id " + s + " is invalid (it should "
              + "match this regex: [A-Za-z0-9\\-\\.]{1,64})"));
        } else if (type.equals("uri")) {
          try {
            URI.create(s);
          } catch (IllegalArgumentException e) {
            errorMessages.add(getAnnotationFromContext(ctx, "URI " + s + " is invalid: " 
                + e.getLocalizedMessage()));
          }
        } else if (type.equals("oid")) {
          try {
            new Oid(s);
          } catch (GSSException e) {
            errorMessages.add(getAnnotationFromContext(ctx, "OID " + s + " is invalid: " 
                + e.getLocalizedMessage()));
          }
        } else if (type.equals("uuid")) {
          try {
            UUID.fromString(s);
          } catch (IllegalArgumentException e) {
            errorMessages.add(getAnnotationFromContext(ctx, "UUID " + s + " is invalid: " 
                + e.getLocalizedMessage()));
          }
        } else if (type.equals("canonical")) {
          // Can have a version using |
          String uri = null;
          if (s.contains("|")) {
            String[] parts = s.split("[|]");
            if (parts.length != 2) {
              errorMessages.add(getAnnotationFromContext(ctx, "Canonical " + s + " is invalid"));
              uri = s; // to continue with validation
            } else {
              uri = parts[0];
            }
          }
          
          try {
            URI.create(uri);
          } catch (IllegalArgumentException e) {
            errorMessages.add(getAnnotationFromContext(ctx, "Canonical " + s + " is invalid: " 
                + e.getLocalizedMessage()));
          }
          
        } else if (type.equals("url")) {
          try {
            new URL(s);
          } catch (MalformedURLException e) {
            errorMessages.add(getAnnotationFromContext(ctx, "URL " + s + " is invalid: " 
                + e.getLocalizedMessage()));
          }
        }
        
      } else if (val instanceof CodeSelectedValue) {
        if (!type.equals("code")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Mapped code", type)));
        }
      } else if (val instanceof ConceptSelectedValue) {
        if (!type.equals("Coding") && !type.equals("CodeableConcept")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Mapped concept", type)));
        }
      } else if (val instanceof ConceptValue) {
        if (!type.equals("Coding") && !type.equals("CodeableConcept")) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Concept", type)));
        }
      }  else if (val instanceof FieldValue) {
        if (Character.isUpperCase(type.charAt(0))) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              String.format(errorMsg, "Value", type)));
        }
      }

      // 4. For references, check that the type of resource referenced in the rules matches the
      // target profile
      if (lastInfo.getType().equals("Reference")) {
        if (val instanceof ReferenceValue) {
          ReferenceValue ref = (ReferenceValue) val;
          String resType = ref.getResourceType();
          
          // Special cases: no target profiles or target profile is Resource
          final List<String> tgtProfiles = lastInfo.getTargetProfiles();
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
              errorMessages.add(getAnnotationFromContext(ctx, "Attribute " + path + " is of "
                  + "type reference but the resource type " + resType + " is incompatible. Valid "
                  + "values are: " + String.join(",", lastInfo.getTargetProfiles())));
            }
          }
        }
      }
    }
  }
  
  private boolean hasErrors() {
    for (Annotation ann : errorMessages) {
      if (ann.getAnnotationType().equals(AnnotationType.ERROR)) {
        return true;
      }
    }
    return false;
  }
  
  /*
   * fcRule
   *     : repeatsClause? condition fcBody (ELSE fcBody)?
   *     ;
   */
  private GrammarObject visitFcRuleInternal(FcRuleContext ctx, Variables var, Metadata meta) {
    final Token start = ctx.getStart();
    final Token stop = ctx.getStop();
    
    int startRow = 0;
    int startCol = 0;
    int endRow = 0;
    int endCol = 0;
    
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
        res.getRules().add(processRule(ctx, startRow, startCol, endRow, endCol, newVar, meta));
      }
      return res;
    } else {
      return processRule(ctx, startRow, startCol, endRow, endCol, var, meta);
    }
  }
  
  private Rule processRule(FcRuleContext ctx, int startRow, int startCol, 
      int endRow, int endCol, Variables var, Metadata meta) {
    final Rule res = new Rule(startRow, startCol, endRow, endCol);
    if (ctx.condition() != null) {
      final Condition c = (Condition) visitConditionInternal(ctx.condition(), var, meta);
      res.setCondition(c);
    } else {
      errorMessages.add(getAnnotationFromContext(ctx, 
          "Expected a condition but it was null."));
    }
    if (ctx.fcBody().size() > 0) {
      res.setBody(visitFcBodyInternal(ctx.fcBody(0), var, meta));
    } else {
      errorMessages.add(getAnnotationFromContext(ctx, 
          "Expected at least one body but found none."));
    }
    if (ctx.fcBody().size() > 1) {
      res.setElseBody(visitFcBodyInternal(ctx.fcBody(1), var, meta));
    }
    return res;
  }
  
  /*
   * fcBody
   *     : '{' (resource | fcRule)* '}'
   *     ;
   */
  private Body visitFcBodyInternal(FcBodyContext ctx, Variables var, Metadata meta) {
    final Body b = new Body();
    
    for(ResourceContext rc :  ctx.resource()) {
      b.getResources().add(visitResourceInternal(rc, var, meta));
    }
    
    for(FcRuleContext rc : ctx.fcRule()) {
      GrammarObject go = visitFcRuleInternal(rc, var, meta);
      if (go instanceof Rule) {
        b.getRules().add((Rule) go);
      } else if (go instanceof RuleList) {
        b.getRules().addAll(((RuleList) go).getRules());
      }
    }
    
    return b;
  }
  
  /*
   * repeatsClause
   *     : 'REPEAT' '(' NUMBER '..' NUMBER ':' IDENTIFIER ')'
   *     ;
   */
  private RepeatsClause visitRepeatsClauseInternal(RepeatsClauseContext ctx) {
    int start = Integer.parseInt(ctx.NUMBER(0).getText());
    int end = Integer.parseInt(ctx.NUMBER(1).getText());
    final String varName = ctx.IDENTIFIER().getText();
    return new RepeatsClause(start, end, varName);
  }

  private String removeEnds(String s) {
    if (s.length() < 2) {
      return s;
    }
    return s.substring(1, s.length() - 1);
  }
  
  /*
   * condition
   *     : '^' condition
   *     | condition '&' condition
   *     | condition '|' condition
   *     | ('TRUE' | 'FALSE')
   *     | ('NULL' | 'NOTNULL') '(' variableIdentifier ')'
   *     | 'VALUE' '(' variableIdentifier ')'('=' | '!=' | '<' | '>' | '<=' | '>=') 
   *          (STRING | NUMBER | 'CONCEPT' '(' CODE ')')
   *     | '(' condition ')'
   *     ;
   *     
   *     Example:
   *     
   *     VALUE(final_diagnosis_num) > 0
   */
  private GrammarObject visitConditionInternal(ConditionContext ctx, Variables var, Metadata meta) { 
    ParseTree first = ctx.getChild(0);
    if (first instanceof TerminalNode) {
      TerminalNode tn = (TerminalNode) first;
      String text = tn.getSymbol().getText();
      if ("^".equals(text)) {
        final Condition c = (Condition) visitConditionInternal(ctx.condition(0), var, meta);
        c.setNegated(true);
        return c;
      } else if ("TRUE".equals(text)) {
        return new ConditionExpression(true);
      } else if ("FALSE".equals(text)) {
        return new ConditionExpression(false);
      } else if ("NULL".equals(text)) {
        VariableIdentifier vi = visitVariableIdentifierInternal(
            ctx.variableIdentifier(), var, false);
        if (!metadata.hasField(vi.getFullId())) {
          errorMessages.add(getAnnotationFromContext(ctx, "Field " + vi.getFullId() 
            + " does not exist in REDCap report."));
        } else {
          return new ConditionExpression(vi.getFullId(), true);
        }
      } else if ("NOTNULL".equals(text)) {
        VariableIdentifier vi = visitVariableIdentifierInternal(
            ctx.variableIdentifier(), var, false);
        if (!metadata.hasField(vi.getFullId())) {
          errorMessages.add(getAnnotationFromContext(ctx, "Field " + vi.getFullId() 
            + " does not exist in REDCap report."));
        } else {
          return new ConditionExpression(vi.getFullId(), false);
        }
      } else if ("VALUE".equals(text)) {
        if (ctx.getChildCount() < 6) {
          errorMessages.add(getAnnotationFromContext(ctx, 
              "Expected at least 6 children but found " + ctx.getChildCount()));
          return null;
        }
        
        VariableIdentifier vi = (VariableIdentifier) visitVariableIdentifierInternal(
            ctx.variableIdentifier(), var, false);
        
        if (!metadata.hasField(vi.getFullId())) {
          errorMessages.add(getAnnotationFromContext(ctx, "Field " + vi.getFullId() 
            + " does not exist in REDCap report."));
        } else {
          String ops = ((TerminalNode) ctx.getChild(4)).getText();
          ConditionExpressionOperator op = getOp(ops);
          if (op == null) {
            errorMessages.add(getAnnotationFromContext(ctx, "Unexpected operator " + ops));
          } else if (ctx.STRING() != null) {
            return new ConditionExpression(vi.getFullId(), op, removeEnds(ctx.STRING().getText()));
          } else if (ctx.NUMBER() != null) {
            String num = ctx.NUMBER().getText();
            if (num.contains(".")) {
              return new ConditionExpression(vi.getFullId(), op, 
                  Double.parseDouble(ctx.NUMBER().getText()));
            } else {
              return new ConditionExpression(vi.getFullId(), op, 
                  Integer.parseInt(ctx.NUMBER().getText()));
            }
          }
        }
      } else if ("(".equals(text)) {
        return (Condition) visitConditionInternal(ctx.condition(0), var, meta);
      } else {
        errorMessages.add(getAnnotationFromContext(ctx, 
            "Expected TRUE, FALSE, NULL, NOTNULL, VALUE or ( but found  " + text));
      }
    } else {
      if (ctx.getChildCount() < 2) {
        errorMessages.add(getAnnotationFromContext(ctx, 
            "Expected at least two children but found " + ctx.getChildCount()));
      }
      ParseTree second = ctx.getChild(1);
      if (second instanceof TerminalNode) {
        TerminalNode tn = (TerminalNode) first;
        String text = tn.getSymbol().getText();
        if ("&".equals(text)) {
          return new ConditionNode((Condition) visitConditionInternal(ctx.condition(0), var, meta),
              ConditionNodeOperator.AND, 
              (Condition) visitConditionInternal(ctx.condition(1), var, meta));
        } else if ("|".equals(text)) {
          return new ConditionNode((Condition) visitConditionInternal(ctx.condition(0), var, meta),
              ConditionNodeOperator.OR, 
              (Condition) visitConditionInternal(ctx.condition(1), var, meta));
        } else {
          errorMessages.add(getAnnotationFromContext(ctx, 
              "Expected & or | but found " + text));
        }
      } else {
        errorMessages.add(getAnnotationFromContext(ctx, 
            "Expected a terminal node but found " + second));
      }
    }
    return new ConditionExpression(false);
  }
  
  private VariableIdentifier visitVariableIdentifierInternal(VariableIdentifierContext ctx, 
      Variables var, boolean inResource) { 
    String id = ctx.IDENTIFIER().get(0).getText();
    
    // Check if this is compliant 
    if (inResource && !idPattern.matcher(id).matches()) {
      errorMessages.add(getAnnotationFromContext(ctx, "FHIR id " + id + " is invalid (it should "
          + "match this regex: [A-Za-z0-9\\-\\.]{1,64})"));
    }
    
    if (ctx.IDENTIFIER().size() > 1) {
      return new VariableIdentifier(id, var.getValue(ctx.IDENTIFIER().get(1).getText()));
    } else {
      return new VariableIdentifier(id);
    }
  }
  
  private ConditionExpressionOperator getOp(String ops) {
    if ("=".equals(ops)) {
      return ConditionExpressionOperator.EQ;
    } else if ("!=".equals(ops)) {
      return ConditionExpressionOperator.NEQ;
    } else if ("<".equals(ops)) {
      return ConditionExpressionOperator.LT;
    } else if (">".equals(ops)) {
      return ConditionExpressionOperator.GT;
    } else if ("<=".equals(ops)) {
      return ConditionExpressionOperator.LTE;
    } else if (">=".equals(ops)) {
      return ConditionExpressionOperator.GTE;
    } else {
      return null;
    }
  }
  
  /*
   * resource
   *   : IDENTIFIER '<' variableIdentifier '>' '->' attribute '=' 
   *        value (',' attribute '=' value)* ';'
   *   
   *   Example: Encounter<final> -> status = "FINISHED";
   */
  private Resource visitResourceInternal(ResourceContext ctx, Variables var, Metadata meta) {
    final Resource res = new Resource();
    if (ctx == null) {
      return res;
    }
    res.setResourceType(ctx.IDENTIFIER().getText());
    
    VariableIdentifier vi = visitVariableIdentifierInternal(ctx.variableIdentifier(), var, true);
    res.setResourceId(vi.getFullId());
    
    for (int i = 0; i < ctx.attribute().size(); i++) {
      AttributeValue av = new AttributeValue();
      av.setAttributes(visitAttributeInternal(ctx.attribute(i)));
      av.setValue(visitValueInternal(ctx.value(i), var));
      res.getResourceAttributeValues().add(av);
    }
    
    validateResource(ctx, res, meta);

    return res;
  }
  
  /*
   * attribute
   *   : IDENTIFIER ('[' NUMBER ']')? ('.' attribute)?
   *   ;
   */
  private List<Attribute> visitAttributeInternal(AttributeContext ctx) {
    final List<Attribute> res = new ArrayList<>();
    while (ctx != null) {
      Attribute att = new Attribute();
      att.setName(ctx.IDENTIFIER().getText());
      if (ctx.NUMBER() != null) {
        att.setAttributeIndex(Integer.parseInt(ctx.NUMBER().getText()));
      }
      res.add(att);
      ctx = ctx.attribute();
    }
    
    // TODO: add validation here, to be able to add errors messages in context
    
    return res;
  }
  
  /*
   * value
   *   : ('TRUE' | 'FALSE')
   *   | STRING
   *   | NUMBER
   *   | reference
   *   | 'CONCEPT_LITERAL' '(' CONCEPT ')'
   *   | 'CODE_LITERAL' '(' CODE ')'
   *   | ('CONCEPT' | 'CONCEPT_SELECTED' | 'CODE_SELECTED' | 'VALUE' | 'LABEL' | 'LABEL_SELECTED' )
   *        '(' variableIdentifier ')'
   *   ;
   */
  private Value visitValueInternal(ValueContext ctx, Variables var) {
    if (ctx.STRING() != null) {
      return new StringValue(removeEnds(ctx.STRING().getText()));
    } else if (ctx.NUMBER() != null) {
      String num = ctx.NUMBER().getText();
      if (num.contains(".")) {
        return new DoubleValue(Double.parseDouble(num));
      } else {
        return new IntegerValue(Integer.parseInt(num));
      }
    } else if (ctx.reference() != null) {
      return visitReferenceInternal(ctx.reference(), var);
    } else if (ctx.CONCEPT_VALUE() != null) {
      String literal = ctx.CONCEPT_VALUE().getText();
      String[] parts = literal.split("[|]");
      if (parts.length == 2) {
        return new ConceptLiteralValue(parts[0], parts[1]);
      } else if(parts.length == 3) {
        return new ConceptLiteralValue(parts[0], parts[1], removeEnds(parts[2]));
      } else {
        throw new CompilerException("Invalid code literal: " + literal 
            + ". This should not happen!");
      }
    } else if (ctx.CODE_VALUE() != null) {
      String literal = ctx.CODE_VALUE().getText();
      return new CodeLiteralValue(literal);
    } else if (ctx.variableIdentifier() != null) {
      String id = visitVariableIdentifierInternal(ctx.variableIdentifier(), var, false).getFullId();
      
      TerminalNode tn = (TerminalNode) ctx.getChild(0);
      String enumConstant = tn.getText();
      if ("CONCEPT".equals(enumConstant)) {
        return new ConceptValue(id);
      } else if("CONCEPT_SELECTED".equals(enumConstant)) {
        return new ConceptSelectedValue(id);
      } else if("CODE_SELECTED".equals(enumConstant)) {
        return new CodeSelectedValue(id);
      } else if("VALUE".equals(enumConstant)) {
        return new FieldValue(id);
      } else {
        throw new CompilerException("Unexpected value type " + enumConstant);
      }
    } else {
      String text = ((TerminalNode) ctx.getChild(0)).getText();
      if ("TRUE".equals(text)) {
        return new BooleanValue(true);
      } else if ("FALSE".endsWith(text)){
        return new BooleanValue(false);
      } else {
        errorMessages.add(getAnnotationFromContext(ctx, 
            "Expected TRUE or FALSE but found " + text));
        return null;
      }
    }
  }
  
  /*
   * reference
   *   : 'REF' '(' IDENTIFIER '<' variableIdentifier '>' ')'
   *   ;
   *   
   * Example: REF(Patient<p>)
   */
  private Value visitReferenceInternal(ReferenceContext ctx, Variables var) {
    final ReferenceValue res = new ReferenceValue();
    res.setResourceType(ctx.IDENTIFIER().getText());
    res.setResourceId(visitVariableIdentifierInternal(
        ctx.variableIdentifier(), var, true).getFullId());
    
    return res;
  }

  private void addError(List<Annotation> errors, String token, int line, int charPositionInLine, String msg) {
    errors.add(new Annotation(line, charPositionInLine, line, charPositionInLine + token.length(),
        msg, AnnotationType.ERROR));
  }
  
  private Annotation getAnnotationFromContext(ParserRuleContext ctx, String  msg) {
    final Token start = ctx.getStart();
    final Token stop = ctx.getStop();
    
    int startRow = 0;
    int startCol = 0;
    int endRow = 0;
    int endCol = 0;
    
    if (start != null) {
      startRow = start.getLine();
      startCol = start.getCharPositionInLine();
    }
    
    if (stop != null) {
      endRow = stop.getLine();
      endCol = stop.getCharPositionInLine() + (stop.getText() != null ? stop.getText().length() : 1);
    } else {
      endRow = startRow;
      endCol = startCol + 1;
    }
    
    return new Annotation(startRow, startCol, endRow, endCol, 
        "Compiler error: " + msg, AnnotationType.ERROR);
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
