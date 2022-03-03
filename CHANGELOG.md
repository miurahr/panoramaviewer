# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- Add file chooser dialog when no file are specified in command line.

### Changed
- Bump versions
  - commons-imaging@1.0-alpha2
  - gradle wrapper@7.3
  - junit@4.13.2
  - joml@1.10.2
- Docs: fix typo

## [0.3] - 2019-08-11
### Added
- CameraPlane: add range check for vectors array index

### Changed
- Rebased on JOSM/Mapillary classes.
- Utilize JOML math functions.
- CameraPlane:
  - change constructor signature
  - change vectors index column order for performance.
  - refactoring and more javadoc.

### Fixed
- CameraPlane: fixed to use ignored `fov` value

## [0.2] - 2018-06-27
### Added
- Use JOML(https://github.com/JOML-CI/JOML) for Math library.
- Enable fastmath algorithm by adding -Djoml:fastmath java option

### Changed
- Moving camera distance calculation to CameraPlane class contructor.
- CameraPlane: Change private method name from getVector3D() to getVector()

### Removed
- Drop Vector3D class, using Vector3d from JOML

## [0.1] - 2018-06-26
- inital release.

[Unreleased]: https://github.com/miurahr/panoramaviewer/compare/v0.2...HEAD
[0.2]: https://github.com/miurahr/panoramaviewer/compare/v0.1...v0.2
