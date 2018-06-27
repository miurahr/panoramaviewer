# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

### Added

### Changed

### Fixed

## [0.2] - 2018-06-27
### Added
- Use JOML(https://github.com/JOML-CI/JOML) for Math library.
- Enable fastmath argorithm by adding -Djoml:fastmath java option

### Changed
- Moving camera distance calculation to CameraPlane class contructor.
- CameraPlane: Change private method name from getVector3D() to getVector()

### Removed
- Drop Vector3D class, using Vector3d from JOML

## [0.1] - 2018-06-26
- inital release.

[Unreleased]: https://github.com/miurahr/panoramaviewer/compare/v0.2...HEAD
[0.2]: https://github.com/miurahr/panoramaviewer/compare/v0.1...v0.2
