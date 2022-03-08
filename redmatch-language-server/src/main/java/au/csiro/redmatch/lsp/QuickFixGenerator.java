/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.compiler.ErrorCodes;
import au.csiro.redmatch.util.DocumentUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.*;

/**
 * Generates quick fixes using the language server protocol.
 *
 * @author Alejandro Metke Jimenez
 */
public class QuickFixGenerator {

  private static final Log log = LogFactory.getLog(LspProgressReporter.class);

  public static List<Either<Command, CodeAction>> computeCodeActions(CodeActionParams params,
                                                                     CancelChecker cancelToken,
                                                                     TextDocumentItem document) {
    String docUri = params.getTextDocument().getUri();
    Map<String, List<Diagnostic>> diagnosticsByType = new HashMap<>();
    for (var diagnostic: params.getContext().getDiagnostics()) {
      String key = diagnostic.getCode().getLeft();
      List<Diagnostic> diagnostics = diagnosticsByType.computeIfAbsent(key, k -> new ArrayList<>());
      diagnostics.add(diagnostic);
    }
    var codeActions = new ArrayList<Either<Command, CodeAction>>();
    for (String key : diagnosticsByType.keySet()) {
      if (cancelToken.isCanceled()) {
        break;
      }
      ErrorCodes errorCode = ErrorCodes.valueOf(key);
      List<Diagnostic> diagnostics = diagnosticsByType.get(key);
      CodeAction codeAction = getCodeAction(errorCode, diagnostics.get(0), docUri, document);
      if (codeAction != null) {
        codeActions.add(Either.forRight(codeAction));
      }
      codeAction = getCodeAction(errorCode, diagnostics, docUri, document);
      if (codeAction != null) {
        codeActions.add(Either.forRight(codeAction));
      }
    }
    return codeActions;
  }

  static CodeAction getCodeAction(ErrorCodes errorCode, Diagnostic diagnostic, String documentUri,
                                  TextDocumentItem document) {
    switch (errorCode) {
      case CODE_INVALID_REDCAP_ID:
      case CODE_UNKNOWN_REDCAP_FIELD:
        return getActionForInvalidRedcapId(diagnostic, documentUri);
      case CODE_INVALID_FHIR_ID:
        return getActionForInvalidFhirId(diagnostic, documentUri);
      case CODE_INVALID_ALIAS:
        return getActionForInvalidAlias(diagnostic, documentUri);
      case CODE_MAPPED_FIELD_DOES_NOT_EXIST:
        return getActionForMappedFieldDoesNotExist(diagnostic, documentUri);
      case CODE_MAPPED_FIELD_LABEL_MISMATCH:
        return getActionForFieldLabelMismatch(diagnostic, documentUri, document);
      case CODE_MAPPING_NOT_NEEDED:
        return getActionForMappingNotNeeded(diagnostic, documentUri);
      default:
        return null;
    }
  }

  static CodeAction getCodeAction(ErrorCodes errorCode, List<Diagnostic> diagnostics, String documentUri,
                                  TextDocumentItem document) {
    switch (errorCode) {
      case CODE_MAPPING_MISSING:
        return getActionForMissingMapping(diagnostics, documentUri, document);
      case CODE_MAPPING_AND_SECTION_MISSING:
        return getActionForMissingMappingAndSection(diagnostics, documentUri, document);
      default:
        return null;
    }
  }

  static CodeAction getActionForInvalidAlias(Diagnostic diagnostic, String documentUri) {
    Object data = diagnostic.getData();
    if (data instanceof JsonObject) {
      JsonObject suggestion = (JsonObject) diagnostic.getData();
      String actual = suggestion.get("actual").getAsString();
      String suggested = suggestion.get("suggested").getAsString();
      String actionLabel = "Replace invalid alias " + actual + " with " + suggested;
      return createCodeAction(diagnostic, suggested, documentUri, actionLabel);
    }
    return null;
  }

  static CodeAction getActionForInvalidFhirId(Diagnostic diagnostic, String documentUri) {
    Object data = diagnostic.getData();
    if (data instanceof JsonObject) {
      JsonObject idSuggestion = (JsonObject) diagnostic.getData();
      String id = idSuggestion.get("actual").getAsString();
      String suggestedId = idSuggestion.get("suggested").getAsString();
      String actionLabel = "Replace invalid FHIR id " + id + " with " + suggestedId;
      return createCodeAction(diagnostic, suggestedId, documentUri, actionLabel);
    }
    return null;
  }

  static CodeAction getActionForInvalidRedcapId(Diagnostic diagnostic, String documentUri) {
    Object data = diagnostic.getData();
    if (data instanceof JsonObject) {
      JsonObject idSuggestion = (JsonObject) diagnostic.getData();
      String id = idSuggestion.get("actual").getAsString();
      String suggestedId = idSuggestion.get("suggested").getAsString();
      String actionLabel = "Replace unknown / invalid field id " + id + " with " + suggestedId;
      return createCodeAction(diagnostic, suggestedId, documentUri, actionLabel);
    }
    return null;
  }

  static CodeAction getActionForMissingMappingAndSection(List<Diagnostic> diagnostics, String documentUri,
                                                         TextDocumentItem document) {
    if (diagnostics == null || diagnostics.isEmpty()) {
      return null;
    }

    Map<String, List<TextEdit>> changes = new HashMap<>();
    List<TextEdit> textEdits = new ArrayList<>();
    changes.put(documentUri, textEdits);

    Diagnostic diagnostic = diagnostics.get(0);
    textEdits.add(new TextEdit(diagnostic.getRange(), document.getText() + "\nMAPPINGS: {\n}"));

    var newCodeAction = new CodeAction("Add mappings section");
    newCodeAction.setKind(CodeActionKind.QuickFix);
    newCodeAction.setDiagnostics(diagnostics);
    newCodeAction.setEdit(new WorkspaceEdit(changes));
    return newCodeAction;
  }

  static CodeAction getActionForMappingNotNeeded(Diagnostic diagnostic, String documentUri) {
    Object data = diagnostic.getData();
    if (data instanceof JsonPrimitive) {
      String fieldId = ((JsonPrimitive) diagnostic.getData()).getAsString();
      String actionLabel = "Remove unnecessary mapping for field " + fieldId;
      return createCodeAction(diagnostic, "", documentUri, actionLabel);
    }
    return null;
  }

  static CodeAction getActionForMissingMapping(List<Diagnostic> diagnostics, String documentUri, TextDocumentItem document) {
    if (diagnostics == null || diagnostics.isEmpty()) {
      return null;
    }

    Map<String, List<TextEdit>> changes = new HashMap<>();
    List<TextEdit> textEdits = new ArrayList<>();
    changes.put(documentUri, textEdits);
    for (Diagnostic diagnostic : diagnostics) {
      Object data = diagnostic.getData();
      if (data instanceof JsonObject) {
        JsonObject labeledField = (JsonObject) diagnostic.getData();
        String fieldId = labeledField.get("id").getAsString();
        String label = labeledField.get("label").getAsString();
        String newValue = getNewValueForMissingMapping(document.getText(), diagnostic.getRange(), fieldId, label);
        textEdits.add(new TextEdit(diagnostic.getRange(), newValue));
      } else {
        log.error("Unexpected data in diagnostic: " + data);
        return null;
      }
    }

    String actionLabel = "Add missing mappings";
    WorkspaceEdit workspaceEdit = new WorkspaceEdit(changes);
    var newCodeAction = new CodeAction(actionLabel);
    newCodeAction.setKind(CodeActionKind.QuickFix);
    newCodeAction.setDiagnostics(diagnostics);
    newCodeAction.setEdit(workspaceEdit);
    return newCodeAction;
  }

  static CodeAction getActionForMappedFieldDoesNotExist(Diagnostic diagnostic, String documentUri) {
    Object data = diagnostic.getData();
    if (data instanceof JsonPrimitive) {
      String fieldId = ((JsonPrimitive) diagnostic.getData()).getAsString();
      String actionLabel = "Remove mapping for non-existent field " + fieldId;
      return createCodeAction(diagnostic, "", documentUri, actionLabel);
    }
    return null;
  }

  static CodeAction getActionForFieldLabelMismatch(Diagnostic diagnostic, String documentUri,
                                                   TextDocumentItem document) {
    Object data = diagnostic.getData();
    if (data instanceof JsonObject) {
      JsonObject labeledField = (JsonObject) diagnostic.getData();
      String fieldId = labeledField.get("id").getAsString();
      String label = labeledField.get("label").getAsString();

      String actionLabel = "Replace label for field " + fieldId;
      String mappingText = DocumentUtils.calculateSnippet(document.getText(), diagnostic.getRange());
      int pipeIndex = mappingText.indexOf("|");
      if (pipeIndex == -1) {
        throw new RuntimeException("Expected a pipe in " + mappingText + ". This should not happen!");
      }
      int firstQuoteIndex = mappingText.indexOf("'", pipeIndex);
      if (firstQuoteIndex == -1) {
        throw new RuntimeException("Expected a single quote in " + mappingText + ". This should not happen!");
      }
      int secondQuoteIndex = mappingText.indexOf("'", firstQuoteIndex + 1);
      if (secondQuoteIndex == -1) {
        throw new RuntimeException("Expected two single quotes in " + mappingText + ". This should not happen!");
      }

      String newValue =
        mappingText.substring(0, firstQuoteIndex + 1)
          + label
          + mappingText.substring(secondQuoteIndex);

      return createCodeAction(diagnostic, newValue, documentUri, actionLabel);
    }
    return null;
  }

  static CodeAction createCodeAction(Diagnostic diagnostic, String newValue, String documentUri, String actionLabel) {
    TextEdit textEdit = new TextEdit(diagnostic.getRange(), newValue);
    Map<String, List<TextEdit>> changes = new HashMap<>();
    changes.put(documentUri, List.of(textEdit));
    WorkspaceEdit workspaceEdit = new WorkspaceEdit(changes);
    var newCodeAction = new CodeAction(actionLabel);
    newCodeAction.setKind(CodeActionKind.QuickFix);
    newCodeAction.setDiagnostics(List.of(diagnostic));
    newCodeAction.setEdit(workspaceEdit);
    return newCodeAction;
  }

  static String getNewValueForMissingMapping(String text, Range range, String fieldId, String label) {
    String mappingsSnippet = DocumentUtils.calculateSnippet(text, range);

    String newMapping = generateDefaultMapping(fieldId ,label);
    // Assuming the mappings section is well-formed, it will end with a '}'
    if (mappingsSnippet.charAt(mappingsSnippet.length() - 1) == '}') {
      return mappingsSnippet.substring(0, mappingsSnippet.length() - 1) + newMapping + "}";
    } else {
      // Otherwise, just add at the end
      return mappingsSnippet + newMapping;
    }
  }

  static String generateDefaultMapping(String fieldId, String label) {
    return "\t" + fieldId + "|'" + label + "' -> http://snomed.info/sct|138875005|'SNOMED CT Concept';\n";
  }
}
