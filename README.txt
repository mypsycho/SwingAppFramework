
This project is an alternative to JSR 296 Application Framework API 
implementation prototype.

Even though, the code was based on the 1.0.3 implementation and beneficiate
of some BASF improvment, this framework is not compatible with former approaches.
 
API has changed, starting with the package name, so migration (and new design)
is required. I hope the client code will be significantly reduced once 
migration perform.

The main idea are:

- No need to invoke resource manager in most case :
  as a component enters in the hierarchie of view, the resource manage is automatically
  called. If the Locale of the component is changed, the content is also updated.
  
- Properties descriptions support nested properties, beans are created by reflection.
  Arrays and maps are supported (see Apache common-beans)
  Ex:
  
  texts(start) = Starting computation
  texts(finish) = Computation performed
  # To create a Map<String, String> with keys "start" and "finish"
  
  icons[0] = ok.png
  icons[1] = ko.png
  icons[2] = abort.png
  # Create an array of 3 icons when the property is typed as Icon[] using the 
  #   containing class as relative path

- No need to declare actions (except for advanced usage) in bean, use on

- MenuBar and Popup Menu can fully defined in properties file

- Application lifecycle are modifiable et extensible

- View strategies are not bound to a type of Application but can be shared

- Resource syntax is extensible

 


The code is organized as a Maven project and it's been built for
JDK 1.6.

* Building the Source Code

The javaws.jar file isn't needed at run-time and is included 
automatically by the Java Web Start launcher.  The pathname for javaws.jar
is specified in nbproject/project.properties, see jnlp.classpath and
osx.jnlp.classpath.  If you're using another IDE, you may need to 
add javaws.jar to the compile-time classpath manually.



