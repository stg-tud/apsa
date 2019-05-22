resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(
  Resolver.ivyStylePatterns
)
addSbtPlugin("org.scoverage"   %% "sbt-scoverage"         % "1.5.1")
addSbtPlugin("org.scalastyle"  %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"           % "2.0.0")

// Dependency management:
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "0.2.10")
addSbtPlugin("com.eed3si9n"     % "sbt-dirty-money"      % "0.2.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.4.0")