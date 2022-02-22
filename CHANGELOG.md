
# Change Log
All notable changes to this project will be documented in this file.

## [2.3.0] - 2022-02-22

- Added support for repeatable instruments in REDCap ([#2](https://github.com/aehrc/redmatch/issues/2)).
- Improved compiler validation of references ([#45](https://github.com/aehrc/redmatch/issues/45)).
- Implemented support for FHIR implementation guides ([#10](https://github.com/aehrc/redmatch/issues/10)).
- Full implementation of auto-complete functionality in language server ([#11](https://github.com/aehrc/redmatch/issues/11)).

## [2.2.0] - 2021-12-16

### Added
- Patial implementation of auto-complete functionality in language server, for REDCap variables only ([#11](https://github.com/aehrc/redmatch/issues/11)).

### Fixed
- Fixed concurrency issues with the diagnostic runner.

## [2.1.0] - 2021-12-07

### Added
- Implemented quick fixes in language server ([#42](https://github.com/aehrc/redmatch/issues/42)).
- Implemented commands in language server ([#43](https://github.com/aehrc/redmatch/issues/43)).

### Fixed
- Fixed location of log file in language server ([#44](https://github.com/aehrc/redmatch/issues/44)).
- Fixed bug where exceptions where being thrown incorrectly when the rules contained fields not in the schema.
