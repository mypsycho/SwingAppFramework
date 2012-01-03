
This project is an alternative to JSR 296 Application Framework API 
implementation prototype.

Even though, the code was based on the 1.0.3 implementation and beneficiate
of some BASF improvment, this framework is not compatible with former approaches.
 
API has changed, starting with the package name, so migration (and new design)
is required. I hope the client code will be significantly reduced once 
migration perform.

The main ideas are:

- No need to invoke resource manager in most case :
  as a component enters in the hierarchy of an application view, the resource 
  manage is automatically called. 
  If the Locale of the component is changed, the content is also updated.
  
- Locale is automatically propagated :
  In AWT, locale is propagate from parent to child
  In Swing, locale is specific to all components. With application in 
  multi-language, it may be a nightmare.
  
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
  #   containing object class as relative path

- No need to declare actions with annotation (except for advanced usage) in bean, 
  simply use property descriptors to reference a method in an action property.

- MenuBar and Popup Menu can be fully defined in properties file.

- Application lifecycles are modifiable and extensible.

- View strategies are not bound to a class of Application but can be shared.

- Property interpretation syntax is extensible, modifiable.

- A lot of annoying details have been cleaned: 
   Strategy for Plateform type, 

- Contains some nice components (Tree Table, CheckBox Tree, auto-fireAction text field)

To be improved:
- Comments are all obsolete.
- Error management in TaskService (and application in general)
- Migrate all examples
- Improve property syntax to have a better reference mechanism (maybe a reflection call) 
- No JUnit
- ...


The code is organized as a Maven project and it's been built for
JDK 1.6.

* Building the Source Code

The javaws.jar file isn't needed at run-time and is included 
automatically by the Java Web Start launcher.  The pathname for javaws.jar
is specified in nbproject/project.properties, see jnlp.classpath and
osx.jnlp.classpath.  If you're using another IDE, you may need to 
add javaws.jar to the compile-time classpath manually.



