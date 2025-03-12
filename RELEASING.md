# Releasing

1. Create a local release branch from main: `git checkout -b vX.Y.Z`
2. Change the version in `config/version.properties` to a non-SNAPSHOT version.
3. Update the `CHANGELOG.md` for the impending release.
4. Update the `README.md` with the new version.
5. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
6. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
7. `git push origin`, open pull-request
8. Merge pull-request and trigger publish workflow in GitHub Actions
9. Visit [Central Sonatype](https://central.sonatype.com/publishing/deployments) and promote the artifact
10. `git checkout main && git pull origin`
11. Update the `config/version.properties` to the next SNAPSHOT version.
12. `git commit -am "Prepare next development version."`
13. `git push && git push --tags`
