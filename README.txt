
This project is an alternative to JSR 296 Application Framework API 
implementation prototype.

Even though, the code was based on the 1.0.3 implementation and beneficiate
of some BASF improvment, this framework is not compatible with former approaches.
 
API has changed, starting with the package name, so migration (and new design)
is required. I hope the client code will be significantly reduced once 
migration performed.


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


- Simple reference and overridable hierarchical references are supported:
  Ex: 
    Reference = Some text
    toolbar[0].button.text = ${Reference}
    menu[0].button.text = ${Reference}
  
    Template.border = empty(5,5,5,5)
    Template.foreground = 100,100,100
    Template.enable = false
    
    components(pane1) = %{Template}
    
    components(pane2) = %{Template}
    components(pane2).border = titled(Some title)

  Is equivalent to:
  
    Reference = Some text
    toolbar[0].button.text = Some text
    menu[0].button.text = Some text
  
    Template.border = empty(5,5,5,5)
    Template.foreground = 100,100,100
    Template.enable = false
    
    components(pane1).border = empty(5,5,5,5)
    components(pane1).foreground = 100,100,100
    components(pane1).enable = false
    
    components(pane2).border = titled(Some title)
    components(pane2).foreground = 100,100,100
    components(pane2).enable = false


- No need to declare actions with annotation (except for advanced usage) in bean, 
  simply use property descriptors to reference a method in an action property.
  Ex:
    components(buttons)(save).action = save
    components(buttons)(save).action.text = &Save
    # Will set an Application Action to the button named 'save'
    #  in a panel named 'button'
    # The application action calls by reflection the method save() of injected object
    

- MenuBar and Popup Menu can be fully defined in properties file using indexed definitions.
  Example for JFrame 
    menuBar[0] = menu
    menuBar[0].text = File
    menuBar[0][0] = item
    menuBar[0][0].action = open
    menuBar[0][0].action.text = Open ...
    menuBar[0][1] = item
    menuBar[0][1].action = save
    menuBar[0][1].action.text = Save
  Creates a menu of 2 buttons 'open' and 'save' in a menu bar.


- Some annoying details have been cleaned: 
    Strategy for Plateform type is open : 
      A default strategy is provided, a user strategy can be defined using java.util.ServiceLoader
    Application lifecycles are modifiable and extensible.
    View strategies are not bound to a class of Application but can be reused.
    Property interpretation syntax is extensible and fully modifiable.
    Property interpretation syntax can call java.lang.System properties.
    Default Application name and vendor are defined using reflection.
    Properties and java files are in the same package.
    Nested classes are supported using the java name (with $ as separator). 

- Contains some nice components: 
    Tree Table, 
    CheckBox Tree, 
    auto-fireAction text field,
    About dialog bound to application properties,
    Help dialog bound to application readme.
    ...
    

Runtime Dependences
    apache-commons-beanutils 1.8.3
    apache-commons-loggings 1.1.1

To be improved:
- Most of comments are obsolete.
- Error management in TaskService (and application in general)
- Not tested : PagedFrame
- Migrate all examples
- Improve property syntax to have a better reference mechanism (maybe a reflection call) 
- Need of JUnit
- ...


The code is organized as a Maven project and it's been built for
JDK 1.6.

* Building the Source Code

The javaws.jar file isn't needed at run-time and is included 
automatically by the Java Web Start launcher.  The pathname for javaws.jar
is specified in nbproject/project.properties, see jnlp.classpath and
osx.jnlp.classpath.  If you're using another IDE, you may need to 
add javaws.jar to the compile-time classpath manually.



