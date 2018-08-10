@ECHO OFF

CMD /C "cd ../gitout.sbt && gradlew"

rm -fr ~/.m2
rm -fr ~/.ivy2
rm -fr ~/.sbt

CMD /C "cd out/gitout/peterlavalle.sbt && sbt publishM2"
CMD /C "cd out/gitout/cgc3.gradle && gradlew publishToMavenLocal"
PAUSE
CMD /C "cd out/gitout/cgc3.gradle/examples/basic && gradlew check"
PAUSE

