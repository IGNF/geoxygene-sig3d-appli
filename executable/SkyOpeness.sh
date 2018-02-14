#!/bin/bash 
JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"
CLASSPATH="geoxygene-sig3d-appli-1.9.jar"
MAIN_CLASS="fr.ign.cogit.exec.SkyOpeness"
java $JAVA_OPTS -cp $CLASSPATH $MAIN_CLASS $@






