
lazy val isRelease =
	System.getProperty("release", "false") match {
		case "true" => true
		case "false" => false
	}

lazy val commonSettings =
	Seq(
		organization := "com.peterlavalle",
		version := {
			"??? unknown???"
		},
		javacOptions ++= Seq("-encoding", "UTF-8"),
		scalaVersion := "2.12.3",
		publishTo := Some(Resolver.file("file", new File("target/m2-repo"))),
		libraryDependencies ++= Seq(
			"junit" % "junit" % "4.12" % Test,
			"org.easymock" % "easymock" % "3.6" % Test
		)
	)

lazy val antlr =
	project
		.settings(
			name := "antlr",
			commonSettings,

			// ... hmm ... seems to do a chicken-or-egg recurisve problem
			libraryDependencies ++= Seq(
				"org.antlr" % "antlr4-runtime" % "4.7",
				"org.antlr" % "antlr4" % "4.7"
			)
		)
		.dependsOn(
			basecode
		)

lazy val basecode =
	project
		.settings(
			name := "peterlavalle",
			commonSettings,

			libraryDependencies ++= Seq(
				"org.codehaus.plexus" % "plexus-utils" % "3.1.0"
			)
		)

lazy val frege =
	project
		.settings(
			name := "peterlavalle",
			commonSettings
		)

lazy val junit =
	project
		.settings(
			name := "junit",
			commonSettings,
			libraryDependencies ++= Seq(
				"junit" % "junit" % "4.12",
				"org.easymock" % "easymock" % "3.5.1"
			)
		)
		.dependsOn(
			basecode
		)

lazy val merc =
	project
		.settings(
			name := "merc",
			commonSettings
		)
		.dependsOn(basecode)

lazy val pcof =
	project
		.settings(
			name := "pcof",
			commonSettings
		)
		.dependsOn(basecode)
		.dependsOn(junit % Test)

lazy val phile =
	project
		.settings(
			name := "phile",
			commonSettings
		)
		.dependsOn(basecode)
		.dependsOn(junit % Test)

lazy val swung =
	(project in file("swung"))
		.settings(
			name := "swung",
			commonSettings
		)
		.dependsOn(
			basecode
		)

lazy val sstate =
	(project in file("sstate"))
		.settings(
			name := "sstate",
			commonSettings
		)

lazy val fudnet =
	(project in file("fudnet"))
		.settings(
			commonSettings
		)
		.dependsOn(
			basecode
		)

lazy val root =
	(project in file("."))
		.aggregate(
			antlr,
			basecode,
			frege,
			fudnet,
			junit,
			merc,
			pcof,
			phile,
			sstate,
			swung
		)
		.settings(
			name := "peterlavalle-root",
			commonSettings
		)
