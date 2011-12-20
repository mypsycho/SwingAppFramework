package org.mypsycho.swing.app.reflect;

import java.awt.Image;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mypsycho.beans.InjectionContext;
import org.mypsycho.beans.converter.AbstractTypeConverter;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class ResourceConverter extends AbstractTypeConverter {

    /**
     *
     */
    public ResourceConverter() {
        super(Icon.class, Image.class, URL.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.converter.TypeConverter#convert(java.lang.Class, java.lang.String,
     * java.lang.Object)
     */
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {
        if ((value == null) || value.isEmpty()) {
            return null;
        }

        URI uri = null;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid uri " + value, e);
        }
        if (URI.class.isAssignableFrom(expected)) {
            return uri;
        }


        URL location = null;

        if (uri.getScheme() != null) {
            try {
                location = uri.toURL();
            } catch (MalformedURLException e) {
                reThrow("Invalid url " + value, e);
            }
        } else {
            Class<?> current = context.getClass();
            if (context instanceof InjectionContext) {
                current = ((InjectionContext) context).getRoot().getClass();
            }
            location = searchResource(uri, current, current.getClassLoader());
        }

        
        if (location == null) {
            return null;
        }

        if (URL.class.isAssignableFrom(expected)) {
            return location;
        }

        if (Icon.class.isAssignableFrom(expected)) {
            return ref(new ImageIcon(location));
        }

        if (Image.class.isAssignableFrom(expected)) {
            return ref(new ImageIcon(location).getImage());
        }

        throw new IllegalArgumentException("Unexpected class " + expected.getName());

    }

    URL searchResource(URI uri, Class<?> current, ClassLoader cl) {

        if (current == null) {
            return null;
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        // else resource
        if (uri.getPath().startsWith("/")) {
            return cl.getResource(uri.getPath().substring(1));
        }

        URI ref = resolvePath(current);
        String path = ref.resolve(uri).getPath(); // Handle parent '..'
        URL res = !path.startsWith("/../") ? cl.getResource(path.substring(1)) : null;
        return (res != null) ? res : searchResource(uri, current.getSuperclass(), cl);
    }

    private URI resolvePath(Class<?> c) {
        while (c.isArray()) {
            c = c.getComponentType();
        }
        String path = "/" + c.getName().replace('.', '/');
        try {
            return new URI(path);
        } catch (URISyntaxException e) { // Impossible ?
            return reThrow("Unknown class name syntax: " + path, e);
        }
    }

    private static <T> Reference<T> ref(T o) {
        return new SoftReference<T>(o);
    }


}
