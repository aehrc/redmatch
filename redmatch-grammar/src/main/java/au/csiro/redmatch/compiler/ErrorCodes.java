package au.csiro.redmatch.compiler;

public enum ErrorCodes {
  CODE_LEXER,
  CODE_PARSER,
  CODE_INVALID_SCHEMA,
  CODE_UNABLE_TO_LOAD_SCHEMA,
  CODE_UNKNOWN_REDCAP_SCHEMA_TYPE,
  CODE_MAPPED_FIELD_DOES_NOT_EXIST,
  CODE_MAPPED_FIELD_LABEL_MISMATCH,
  CODE_MAPPING_NOT_NEEDED,
  CODE_MAPPING_MISSING,
  CODE_MAPPING_AND_SECTION_MISSING,
  CODE_UNSUPPORTED_SCHEMA,
  CODE_COMPILER_ERROR,
  CODE_UNKNOWN_VARIABLE,
  CODE_INVALID_REDCAP_ID,
  CODE_UNKNOWN_REDCAP_FIELD,
  CODE_INVALID_FHIR_ID,
  CODE_INVALID_FHIR_ATTRIBUTE_PATH,
  CODE_FHIR_ATTRIBUTE_NOT_ALLOWED,
  CODE_INVALID_FHIR_ATTRIBUTE_INDEX,
  CODE_INCOMPATIBLE_TYPE,
  CODE_INVALID_DATE_PRECISION,
  CODE_INVALID_URI,
  CODE_INVALID_OID,
  CODE_INVALID_UUID,
  CODE_INVALID_CANONICAL,
  CODE_INVALID_URL,
  CODE_INCOMPATIBLE_EXPRESSION,
  CODE_INVALID_ALIAS,
  CODE_INVALID_REFERENCE_TYPE
}