# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

- Checkbox in the GUI for applying the configuration file is hidden at the moment in the gui but is exposed in the api. 

## [0.1.3] - 2017-02-06

### Added

- Integration/functional tests against docker image.

### Changed

- Spring Boot 2.0.0.RC1

### Removed

- Disable and/or removed any actuator stuff.
- Removed ili/toml files of temporary models (Nutzungsplanung).

## [0.1.2] - 2017-11-21

- ??

## [0.1.1] - 2017-09-20

### Changed

- Fixed some typos in `SO_Nutzungsplanung_20170915_Validierung_20170915.ili`.

## [0.1.0] - 2017-09-16

### Added

- ilivalidator configuration file (aka `toml`) support. One configuration file for one INTERLIS model only (baked into resources directory). It will be used by default if one is present. It looks for a *.toml file with the same name as the data model name. The configuration file name must be lower-case.
- Added and adjusted some scripts for AWS Codepipeline with deployment on EBS.
- Add Spring Boot Actuators with a custom endpoint for showing the application's classpath.
- INTERLIS models can be stored in `src/main/resources/ili`. These models will be copied into the folder where the transfer file stored and will be picked up with highest priority.

### Changed

- Spring Boot 1.5.6
- ilivalidator-1.4.0
- Use sogeo.services artifactory repository instead of static maven repository.
- Use `java.io.tmpdir` as default temporary directory. Can be overriden with the `ch.so.agi.ilivalidator.uploadedFiles` in the `application.properties` file.
- Result output is now UTF-8.

