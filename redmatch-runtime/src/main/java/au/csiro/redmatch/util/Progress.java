/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

/**
 * Used to report progress.
 *
 * @see ProgressReporter
 *
 * @author Alejandro Metke Jimenez
 */
public class Progress {
  public enum Stage { START, PROGRESS, END }

  private final Stage stage;
  private final int percentage;
  private final String message;

  private Progress (Stage stage, int percentage, String message) {
    this.stage = stage;
    this.percentage = percentage;
    this.message = message;
  }

  private Progress (Stage stage, String message) {
    this(stage, 0, message);
  }

  private Progress (Stage stage, int percentage) { this(stage, percentage, null); }

  private Progress (Stage stage) { this(stage, 0, null); }

  public static Progress reportStart(String message) {
    return new Progress(Stage.START, message);
  }

  public static Progress reportProgress(int percentage) {
    return new Progress(Stage.PROGRESS, percentage);
  }

  public static Progress reportEnd() {
    return new Progress(Stage.END);
  }

  public Stage getStage() {
    return stage;
  }

  public int getPercentage() {
    return percentage;
  }

  public String getMessage() {
    return message;
  }
}
