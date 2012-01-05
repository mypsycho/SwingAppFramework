/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class InjectionTemplate {
    
    protected static final String PLAIN_START = "%{";
    protected static final String PLAIN_END = "}";
    
    protected static final String ANY = "(.*)";
    protected static final String START = "^%\\{";
    protected static final String END = "\\}$";
    protected static final String LITERAL = "(\\w+)";
    protected static final String PATH = '(' + LITERAL + TIME(OR("\\." + LITERAL, 
            "\\(" + LITERAL + "\\)", "\\[\\d+\\]"), "*") + ')';
    
    protected static final String ARG = "\\{" + LITERAL + "=" + ANY + "\\}";
    protected static final String VALUE = "=" + ANY;
            
    protected static final String REG = START + PATH + TIME(ARG, "*") + TIME(VALUE, "?") + END;
    protected Pattern pattern = Pattern.compile(REG);
    protected Pattern argsPattern = Pattern.compile("\\}\\{");
    protected Pattern argPattern = Pattern.compile("\\=");
    
    protected static String OR(String... regs) {
        String or = null;
        for (String reg : regs) {
            or = (or == null) ? ("(" + reg + ")") : (or + "|(" + reg + ")");
        }
        return "(" + or + ")";
    }
    
    protected static String TIME(String reg, String time) {
        if (time.charAt(0) != '(') {
            reg = "(" + reg + ")";
        }
        return reg + time;
    }

    
    String name;
    
    String value;
    
    Map<String, String> options;
    
    public InjectionTemplate() {
    }
    
    protected InjectionTemplate(String n, String v, String[][] args) {
        name = n;
        value = v;
        if ((args != null) && (args.length > 0)) {
            options = new HashMap<String, String>();
            for (String[] arg : args) {
                options.put(arg[0], arg[1]);
            }
            if (options.size() != args.length) {
                throw new IllegalArgumentException("Duplicated parameter");
            }
        }
    }
    
    
    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    
    /**
     * Returns the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    
    /**
     * Returns the options.
     *
     * @return the options
     */
    public Map<String, String> getOptions() {
        return options;
    }

    protected String[] validArg(String[] arg) throws IllegalArgumentException {
        if ((arg == null) || (arg.length == 0)) {
            throw new IllegalArgumentException("Missing Argument");
        }

        if (arg.length == 1) {
            arg = new String[] { arg[0], "" };
        }
        if (arg[0].isEmpty()) {
            throw new IllegalArgumentException("Missing Argument");
        }
        return arg;
    }
    
    protected String optionTag(String option) {
        return "{" + option + "}";
    }
    
    protected String fullName(String s, String[][] args) {
        if (args == null) {
            return s;
        }
        
        String options = "";
        for (String[] arg : args) {
            options += optionTag(arg[0]);
        }
 
        return s + options;
    }
    
    public InjectionTemplate parse(String s) {
        if ((s == null) 
                || (s.length() < PLAIN_START.length() + PLAIN_END.length() + 1)
                || !s.startsWith(PLAIN_START) || !s.endsWith(PLAIN_END)) {
            return null;
        }
        Matcher matcher = pattern.matcher(s);
        boolean found = matcher.find();

        if (!found) {
            return null;
        }

        String value = matcher.group(13);

        if (value != null) {
            value = value.substring(1); // ignore '='
        }
        
        try {
            String[][] args = args(matcher.group(10));
            return createTemplate(fullName(matcher.group(1), args), value, args);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    protected InjectionTemplate createTemplate(String n, String v, String[][] args) {
        return new InjectionTemplate(n, value, args);
    }
    
    
    protected String[][] args(String s) throws IllegalArgumentException {
        if (s == null) {
            return null;
        }
        s = s.substring(1, s.length() - 1);
        String[] elements = argsPattern.split(s);
        String[][] args = new String[elements.length][];
        for (int i = 0; i < elements.length; i ++) {
            args[i] = validArg(argPattern.split(elements[i], 2));
        }
        return args;
    }

    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     * @param definition
     * @return
     */
    public String substitut(String definition) {
        if (definition == null) {
            return null;
        }
        if (options == null) {
            return definition;
        }

        for (Map.Entry<String, String> opt : options.entrySet()) {
            definition = definition.replace(optionTag(opt.getKey()), opt.getValue());
        }

        return definition;
    }

    
    
}

