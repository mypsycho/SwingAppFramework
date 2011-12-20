package org.mypsycho.swing.app.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.mypsycho.beans.converter.AbstractTypeConverter;
import org.mypsycho.beans.converter.TypeConverter;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class BorderConverter extends AbstractTypeConverter {

    TypeConverter delegate;

    public BorderConverter(TypeConverter d) {
        super(Border.class);
        delegate = d;
    }

    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {

        List<String> call = decode(value);
        
        if ("titled".equals(call.get(0)) && call.size() == 2) {
            // Ambiguous case
            return BorderFactory.createTitledBorder(call.get(1));
        }
        
        
        Method create = findCreateMethod(call);

        Class<?>[] argTypes = create.getParameterTypes();
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = delegate.convert(argTypes[i], call.get(i + 1), context);
        }
        try {
            return create.invoke(null, args);
        } catch (Exception e) {
            return reThrow("Cannot create border " + value, e);
        }
    }

    protected Method findCreateMethod(List<String> call) {
        String methodName = call.get(0);
        methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        methodName = "create" + methodName + "Border";
        
        for (Method method : BorderFactory.class.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    || !Modifier.isPublic(method.getModifiers())
                    || !methodName.equals(method.getName())) {
                continue;
            }
            if (method.getParameterTypes().length == call.size() - 1) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown Border method " + methodName);
    }

    protected List<String> decode(String pattern) {
        List<String> invocation = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();

        int braceStack = 0;

        for (int i = 0; i < pattern.length(); ++i) {
            char ch = pattern.charAt(i);
            if (ch == '(') {
                if (invocation.isEmpty()) {
                    invocation.add(buf.toString().trim());
                    buf.setLength(0);
                } else {
                    braceStack++;
                    buf.append(ch);
                }
            } else if (ch == ',') {
                if (invocation.isEmpty()) {
                    throw new IllegalArgumentException("unexpected separator at " + i);
                }
                if (braceStack == 0) {
                    invocation.add(buf.toString().trim());
                    buf.setLength(0);
                } else {
                    buf.append(ch);
                }
            } else if (ch == ')') {
                if (braceStack == 0) {
                    String end = pattern.substring(i).trim();
                    if (invocation.isEmpty() && !end.isEmpty()) {
                        throw new IllegalArgumentException("unexpected end: " + end);
                    }
                    invocation.add(buf.toString().trim());
                    buf.setLength(0);
                } else {
                    braceStack--;
                    buf.append(ch);
                }

            } else {
                buf.append(ch);
            }
        }
        if (invocation.isEmpty()) {
            invocation.add(buf.toString().trim());
            buf.setLength(0);
        }
        if ((braceStack > 0) || (buf.length() > 0)) {
            throw new IllegalArgumentException("unexpected end");
        }

        for (int i = 1; i < invocation.size(); i++) {
            String arg = invocation.get(i);
            while ((arg.charAt(0) == '(') && (arg.charAt(arg.length() - 1) == ')')) {
                arg = arg.substring(0, arg.length() - 1);
            }
            invocation.set(i, arg);
        }

        return invocation;
    }

    
    

}
