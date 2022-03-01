/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp.completion;

import au.csiro.redmatch.grammar.RedmatchLexer;
import au.csiro.redmatch.lsp.RedmatchTextDocumentService;
import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.DocumentUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.hl7.fhir.r4.model.ValueSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generates auto-completions.
 *
 * @author Alejandro Metke-Jimenez
 *
 */
public class CompletionProcessor {

  /** Logger. */
  private static final Log log = LogFactory.getLog(CompletionProcessor.class);

  private final RedmatchTextDocumentService documentService;
  private final TerminologyService terminologyService;

  public CompletionProcessor(RedmatchTextDocumentService documentService, TerminologyService terminologyService) {
    this.documentService = documentService;
    this.terminologyService = terminologyService;
  }

  /**
   * Returns a list of possible auto-completions.
   *
   * @param url The document url.
   * @param document The text of the document.
   * @param position The position in the text where the autocompletion was triggered.
   * @return A list of possible auto-completions.
   */
  public synchronized List<CompletionItem> getCompletions(String url, String document, Position position) {
    String snippet = document.substring(0, DocumentUtils.getPosition(position, document));
    if (snippet.length() >= 6) {
      log.debug("Processing snippet ending in " + snippet.substring(snippet.length() - 6));
    } else {
      log.debug("Processing snippet " + snippet);
    }

    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(snippet));
    List<? extends Token> tokens = lexer.getAllTokens();

    List<CompletionItem> completionItems = handleOpen(url, tokens);
    if (!completionItems.isEmpty()) {
      return completionItems;
    }

    completionItems = handleResource(url, tokens);
    if (!completionItems.isEmpty()) {
      return completionItems;
    }

    completionItems = handleAttribute(url, tokens);

    return completionItems;
  }

  /**
   * Handles the generation of autocompletion items after the opening bracket for the following keywords:
   *
   * <ul>
   *   <li>NULL</li>
   *   <li>NOTNULL</li>
   *   <li>VALUE</li>
   *   <li>CONCEPT</li>
   *   <li>CONCEPT_SELECTED</li>
   *   <li>CODE_SELECTED</li>
   * </ul>
   *
   * @param url The document url.
   * @param tokens The list of tokens
   * @return List of possible completions.
   */
  private List<CompletionItem> handleOpen(String url, List<? extends Token> tokens) {
    Token previousToken = null;
    Token idToken = null;

    Token last = tokens.get(tokens.size() - 1);
    switch (last.getType()) {
      case RedmatchLexer.CLOSE:
        if (tokens.size() >= 4) {
          // Look for token before close
          Token beforeClose = tokens.get(tokens.size() - 2);
          if (beforeClose.getType() == RedmatchLexer.ID) {
            // Look for token before id
            Token beforeId = tokens.get(tokens.size() - 3);
            if (beforeId.getType() == RedmatchLexer.OPEN) {
              // Look for token before open
              previousToken = tokens.get(tokens.size() - 4);
              idToken = beforeClose;
            } else {
              return Collections.emptyList();
            }
          } else {
            return Collections.emptyList();
          }
        }
        break;
      case RedmatchLexer.ID:
        if (tokens.size() >= 3) {
          // Look for token before id
          Token beforeId = tokens.get(tokens.size() - 2);
          if (beforeId.getType() == RedmatchLexer.OPEN) {
            // Look for token before open
            previousToken = tokens.get(tokens.size() - 3);
            idToken = tokens.get(tokens.size() - 1);
          } else {
            return Collections.emptyList();
          }
        } else {
          return Collections.emptyList();
        }
        break;
      case RedmatchLexer.OPEN:
        if (tokens.size() >= 2) {
          // Look for token before open
          previousToken = tokens.get(tokens.size() - 2);
        } else {
          return Collections.emptyList();
        }
        break;
      default:
        return Collections.emptyList();
    }

    String prefix = idToken != null ? idToken.getText() : null;
    switch (Objects.requireNonNull(previousToken).getType()) {
      case RedmatchLexer.NULL:
      case RedmatchLexer.NOTNULL:
      case RedmatchLexer.VALUE:
      case RedmatchLexer.CONCEPT:
      case RedmatchLexer.CONCEPT_SELECTED:
      case RedmatchLexer.CODE_SELECTED:
        // In any of these cases we return a list of fields in the schema
        Schema schema = documentService.getSchema(url);
        if (schema != null) {
          log.debug("Found cached schema for document " + url + " with " + schema.getFields().size() + " fields");
          if (prefix != null && !prefix.isEmpty()) {
            return documentService.getSchema(url).getFields().stream()
              .map(f -> {
                CompletionItem completionItem = new CompletionItem(f.getFieldId());
                completionItem.setDetail(f.getType());
                completionItem.setDocumentation(f.getLabel());
                return completionItem;
              })
              .filter(f -> f.getLabel().toLowerCase().startsWith(prefix.toLowerCase()))
              .collect(Collectors.toList());
          } else {
            return documentService.getSchema(url).getFields().stream()
              .map(f -> {
                CompletionItem completionItem = new CompletionItem(f.getFieldId());
                completionItem.setDetail(f.getType());
                completionItem.setDocumentation(f.getLabel());
                return completionItem;
              })
              .collect(Collectors.toList());
          }
        } else {
          log.warn("Schema for document " + url + " was not found");
        }
    }
    return Collections.emptyList();
  }

  /**
   * Handles searching for a FHIR resource name.
   *
   * @param url The document url. Used to get the FHIR package for that document.
   * @param tokens The list of tokens.
   * @return A list of potential completions.
   */
  private List<CompletionItem> handleResource(String url, List<? extends Token> tokens) {
    List<CompletionItem> completionItems = new ArrayList<>();

    if (tokens.size() <= 3) {
      return completionItems;
    }

    // Determine if this is an auto-completion of a resource
    Token last = tokens.get(tokens.size() - 1);
    Token beforeLast = tokens.get(tokens.size() - 2);
    Token beforeBeforeLast = tokens.get(tokens.size() - 3);

    if (last.getType() == RedmatchLexer.ID && beforeLast.getType() == RedmatchLexer.OPEN_CURLY &&
      beforeBeforeLast.getType() != RedmatchLexer.COLON) {
      return handleResource(url, last.getText());
    } else if (last.getType() == RedmatchLexer.OPEN_CURLY && beforeLast.getType() != RedmatchLexer.COLON) {
      return handleResource(url, "");
    } else {
      return completionItems;
    }
  }

  private List<CompletionItem> handleResource(String url, String prefix) {
    VersionedFhirPackage fhirPackage = documentService.getFhirPackage(url);
    if (fhirPackage == null) {
      return Collections.emptyList();
    }
    try {
      List<CompletionItem> completionItems = new ArrayList<>();
      ValueSet expansion = terminologyService.expand(fhirPackage, prefix, true, null);
      for (ValueSet.ValueSetExpansionContainsComponent component : expansion.getExpansion().getContains()) {
        completionItems.add(new CompletionItem(component.getCode()));
      }
      return completionItems;
    } catch (IOException e) {
      log.error("There was a problem creating auto-completion results for document " + url + " and prefix " + prefix);
      return Collections.emptyList();
    }
  }

  /**
   * Handles searching for a FHIR resource attribute name.
   *
   * @param url The document url. Used to get the FHIR package for that document.
   * @param tokens The list of tokens.
   * @return A list of potential completions.
   */
  private List<CompletionItem> handleAttribute(String url, List<? extends Token> tokens) {
    List<CompletionItem> completionItems = new ArrayList<>();

    // Look for ATTRIBUTE_START and return if we find a token that is not one of the following: NOT, PATH, OPEN_SQ,
    // INDEX, CLOSE_SQ or ATTRIBUTE_START
    List<Token> attributes = new ArrayList<>();
    int i = tokens.size() - 1;
    outer:
    for (; i >= 0; i--) {
      switch(tokens.get(i).getType()) {
        case RedmatchLexer.ATTRIBUTE_START:
          break outer;
        case RedmatchLexer.NOT:
        case RedmatchLexer.PATH:
        case RedmatchLexer.OPEN_SQ:
        case RedmatchLexer.INDEX:
        case RedmatchLexer.CLOSE_SQ:
        case RedmatchLexer.DOT:
          attributes.add(tokens.get(i));
          break;
        default:
          return completionItems;
      }
    }

    // We need to look for the resource token, because it is needed in the call to Ontoserver. This will be the second
    // ID token after the colon, going from right to left
    Token resourceToken = null;
    boolean hasSeenColon = false;
    boolean hasSeenResourceId = false;
    outer:
    for (; i >= 0; i--) {
      switch(tokens.get(i).getType()) {
        case RedmatchLexer.COLON:
          hasSeenColon = true;
          break;
        case RedmatchLexer.ID:
          if (hasSeenColon) {
            if (hasSeenResourceId) {
              resourceToken = tokens.get(i);
              break outer;
            } else {
              hasSeenResourceId = true;
            }
          }
          break;
        default:
          break;
      }
    }

    if (resourceToken == null) {
      return completionItems;
    }

    // The attributes array contains the tokens that make up the prefix
    StringBuilder sb = new StringBuilder();
    for (i = attributes.size() - 1; i >= 0; i--) {
      sb.append(attributes.get(i).getText());
    }
    String prefix = sb.toString();
    VersionedFhirPackage fhirPackage = documentService.getFhirPackage(url);
    if (fhirPackage == null) {
      return Collections.emptyList();
    }
    try {
      String parentResource = resourceToken.getText();
      ValueSet expansion = terminologyService.expand(fhirPackage, prefix, false, parentResource);
      for (ValueSet.ValueSetExpansionContainsComponent component : expansion.getExpansion().getContains()) {
        String code = component.getCode().substring(parentResource.length() + 1); // +1 to account for the dot
        completionItems.add(new CompletionItem(code));
      }
      return completionItems;
    } catch (IOException e) {
      log.error("There was a problem creating auto-completion results for document " + url + " and prefix " + prefix);
      return Collections.emptyList();
    }
  }

}
