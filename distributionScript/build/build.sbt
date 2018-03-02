name := "openmole-geoxygene-sig3d-appli"

version := "1.0"

scalaVersion := "2.12.4"

enablePlugins(SbtOsgi)

osgiSettings

OsgiKeys.exportPackage := Seq("openmole")

OsgiKeys.importPackage := Seq("*;resolution:=optional")

OsgiKeys.privatePackage := Seq("!scala.*,!java.*,*", "META-INF.services.*", "META-INF.*")

OsgiKeys.requireCapability := """osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.8))""""

/*
scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
*/

resolvers += Resolver.mavenLocal

resolvers += "IDB" at "http://igetdb.sourceforge.net/maven2-repository/"

resolvers += "IGN snapshots" at "https://forge-cogit.ign.fr/nexus/content/repositories/snapshots/"

resolvers += "IGN releases" at "https://forge-cogit.ign.fr/nexus/content/repositories/releases/"

resolvers += "ImageJ" at "http://maven.imagej.net/content/repositories/public"

resolvers += "Boundless" at "http://repo.boundlessgeo.com/main"

resolvers += "osgeo" at "http://download.osgeo.org/webdav/geotools/"

resolvers += "geosolutions" at "http://maven.geo-solutions.it/"

resolvers += "Hibernate" at "http://www.hibernatespatial.org/repository"

libraryDependencies += "fr.ign.cogit" % "geoxygene-sig3d-appli" % "1.9-SNAPSHOT"

