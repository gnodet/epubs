/*
 * #%L
 * Docbkx Maven Plugin
 * %%
 * Copyright (C) 2006 - 2014 Wilfred Springer, Cedric Pronzato
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.gnodet.epubs.plugin;

import com.agilejava.docbkx.maven.AbstractTransformerMojo;
import com.agilejava.docbkx.maven.PreprocessingFilter;
import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.xinclude.XIncludeException;
import nu.xom.xinclude.XIncluder;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The base class of all other mojos. Introduced to add some common behaviour, outside of the
 * {@link AbstractTransformerMojo}.
 *
 * @author Wilfred Springer
 */
public abstract class AbstractMojoBase extends AbstractTransformerMojo {
    @Override
    protected void setProperty(String propertyname, String value) {
        try {
            final Field f = this.getClass().getDeclaredField(propertyname);
            f.setAccessible(true);
            if (f.getType().equals(Boolean.class)) {
                f.set(this, convertBooleanToXsltParam(value));
            } else {
                f.set(this, value);
            }
        } catch (NoSuchFieldException e) {
            getLog().warn("Property not found in " + this.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            getLog().warn("Unable to set " + propertyname + " value", e);
        }
    }

    @Override
    protected String getProperty(String propertyname) {
        try {
            final Field f = this.getClass().getDeclaredField(propertyname);
            f.setAccessible(true);
            Object o = f.get(this);
            if (o == null) {
                return null;
            } else {
                return o.toString();
            }
        } catch (NoSuchFieldException e) {
            getLog().warn("Property not found in " + this.getClass().getName());
        } catch (IllegalAccessException e) {
            getLog().warn("Unable to get " + propertyname + " value");
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MojoExecutionException DOCUMENT ME!
     */
    public void preProcess() throws MojoExecutionException {
        super.preProcess();
        configureXslthl();
    }

    private void configureXslthl() {
        URL url = this.getClass().getClassLoader().getResource("docbook/highlighting/xslthl-config.xml");

        final String config = getProperty("highlightXslthlConfig");
        final String xslthlSysProp = System.getProperty("xslthl.config");

        if (config != null) {
            url = convertToUrl(config);
        } else if (xslthlSysProp != null) {
            // fallback on system property as in previous version of xslthl
            url = convertToUrl(xslthlSysProp);
        }

        // else using config file provided in the release
        if (url == null) {
            getLog().error("Error while converting XSLTHL config file");
        } else {
            setProperty("highlightXslthlConfig", url.toExternalForm());
        }
    }

    /**
     * Converts a conventional path to url format bzcause XSLTHL only takes as input a
     * configuration file path given as an url.
     *
     * @param path The path to format.
     * @return The formated path or null if an error occurred.
     * @throws IllegalArgumentException If the input path is null.
     */
    private URL convertToUrl(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Config file path must not be null");
        }

        final String s = path.replace("file:///", "/");
        final File file = new File(s);

        if (!file.exists() || !file.isFile() || !file.canRead()) {
            getLog().warn("The given XSLTHL config file seems to not be legal: " + path);
        } else {
            try {
                return file.toURL();
            } catch (MalformedURLException e) {
                getLog().error(e);
            }
        }

        return null;
    }

    protected Source createSource(String inputFilename, File sourceFile, PreprocessingFilter filter)
            throws MojoExecutionException {
        // if both properties are set, XOM is used for a better XInclude support.
        if (getXIncludeSupported() && getGeneratedSourceDirectory() != null) {
            getLog().debug("Advanced XInclude mode entered");
            final Builder xomBuilder = new Builder();
            try {
                final nu.xom.Document doc = xomBuilder.build(sourceFile);
                XIncluder.resolveInPlace(doc);
                // TODO also dump PIs computed and Entities included
                final File dump = dumpResolvedXML(inputFilename, doc);
                return new SAXSource(filter, new InputSource(dump.getAbsoluteFile().toURI().toString()));
            } catch (ValidityException e) {
                throw new MojoExecutionException("Failed to validate source", e);
            } catch (ParsingException e) {
                throw new MojoExecutionException("Failed to parse source", e);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to read source", e);
            } catch (XIncludeException e) {
                throw new MojoExecutionException("Failed to process XInclude", e);
            }
        } else { // else fallback on Xerces XInclude support.
            getLog().debug("Xerces XInclude mode entered");
            final InputSource inputSource = new InputSource(sourceFile.getAbsoluteFile().toURI().toString());
            return new SAXSource(filter, inputSource);
        }
    }

}
