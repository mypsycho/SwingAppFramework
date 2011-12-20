
This project is an alternative to JSR 296 Application Framework API 
implementation prototype.

Eventhrough, the code was based on the 1.0.3 implementation and beneficiate
of BASF improvment, this framework is not compatible with former approach.
 
API has changed, starting with the package name, so migration (and rethinking)
is required. I hope the client code will be significantly reduced once 
migration perform.


The code is organized as a Maven project and it's been built for
JDK 1.6.

* Building the Source Code

The javaws.jar file isn't needed at run-time and is included 
automatically by the Java Web Start launcher.  The pathname for javaws.jar
is specified in nbproject/project.properties, see jnlp.classpath and
osx.jnlp.classpath.  If you're using another IDE, you may need to 
add javaws.jar to the compile-time classpath manually.



