/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp.completion;

import au.csiro.redmatch.grammar.RedmatchLexer;
import au.csiro.redmatch.lsp.RedmatchTextDocumentService;
import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.util.DocumentUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.util.Collections;
import java.util.List;
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

  public CompletionProcessor(RedmatchTextDocumentService documentService) {
    this.documentService = documentService;
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
    String snippet = document.substring(0, DocumentUtils.getPosition(position, document) + 1);
    if (snippet.length() >= 6) {
      log.info("Processing snippet ending in " + snippet.substring(snippet.length() - 6));
    } else {
      log.info("Processing snippet " + snippet);
    }

    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(snippet));
    List<? extends Token> tokens = lexer.getAllTokens();

    Token last = tokens.get(tokens.size() - 1);
    switch (last.getType()){
      // Last token is a '{'
      case RedmatchLexer.OPEN_CURLY:
        // The only case when an open curly is not preceded by a colon is the case we want
        if (tokens.size() >= 2) {
          Token beforeCurly = tokens.get(tokens.size() - 2);
          if (beforeCurly.getType() != RedmatchLexer.COLON) {
            // Return all resource types if user hasn't started typing
            return handleResource(url, null);
          }
        }
      case RedmatchLexer.CLOSE:
        if (tokens.size() >= 4) {
          // Look for token before close
          Token beforeClose = tokens.get(tokens.size() - 2);
          if (beforeClose.getType() == RedmatchLexer.ID) {
            // Look for token before id
            Token beforeId = tokens.get(tokens.size() - 3);
            if (beforeId.getType() == RedmatchLexer.OPEN) {
              // Look for token before open
              Token beforeOpen = tokens.get(tokens.size() - 4);
              return handleOpen(url, beforeOpen, beforeClose);
            }
          }
        }
        break;
      case RedmatchLexer.ID:
        if (tokens.size() >= 3) {
          // Look for token before id
          Token beforeId = tokens.get(tokens.size() - 2);
          if (beforeId.getType() == RedmatchLexer.OPEN) {
            // Look for token before open
            Token beforeOpen = tokens.get(tokens.size() - 3);
            return handleOpen(url, beforeOpen, tokens.get(tokens.size() - 1));
          }
        }
        break;
      case RedmatchLexer.OPEN:
        if (tokens.size() >= 2) {
          // Look for token before open
          Token beforeOpen = tokens.get(tokens.size() - 2);
          return handleOpen(url, beforeOpen, null);
        }
        break;
    }
    return Collections.emptyList();
  }

  /**
   * Handles the generation of autocompletion items for the several keywords.
   *
   * @param url The document url.
   * @param beforeOpen The token before the opening parenthesis.
   * @param idToken The id token, i.e., the text the user has started typing.
   * @return List of possible completions.
   */
  private List<CompletionItem> handleOpen(String url, Token beforeOpen, Token idToken) {
    String prefix = idToken != null ? idToken.getText() : null;
    switch (beforeOpen.getType()) {
      case RedmatchLexer.NULL:
      case RedmatchLexer.NOTNULL:
      case RedmatchLexer.VALUE:
      case RedmatchLexer.CONCEPT:
      case RedmatchLexer.CONCEPT_SELECTED:
      case RedmatchLexer.CODE_SELECTED:
        // In any of these cases we return a list of fields in the schema
        Schema schema = documentService.getSchema(url);
        if (schema != null) {
          log.info("Found cached schema for document " + url + " with " + schema.getFields().size() + " fields");
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

  private List<CompletionItem> handleResource(String url, Token resourceToken) {
    // TODO: finish this!
    return Collections.emptyList();
  }

}
