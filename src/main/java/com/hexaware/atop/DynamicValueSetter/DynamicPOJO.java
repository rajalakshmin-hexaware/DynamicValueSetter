package com.hexaware.atop.DynamicValueSetter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;


import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaFileManager.Location;

public class DynamicPOJO {

    public  Object createStudent(String classname) throws Exception {
    Object student = null;
    Map<String,byte[]> byteObj = createByteCode();
    if(classname.equals("test.Model")) {
        
        JavaFileObject jo = new SimpleJavaFileObject(URI.create("runtime:///test/Model.class"), Kind.CLASS) {
        @Override
        public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(byteObj.get("test.Model"));
        }
    };
        ClassLoader classLoader = new MyClassLoader(jo,byteObj);
        Class<?> cl = classLoader.loadClass("test.Model");
        
        student=cl.getConstructor().newInstance();
        
    }
    else if(classname.equals("test.Tag")) {
        JavaFileObject jo = new SimpleJavaFileObject(URI.create("runtime:///test/Tag.class"), Kind.CLASS) {
        @Override
        public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(byteObj.get("test.Tag"));
        }
    };
        ClassLoader classLoader = new MyClassLoader(jo,byteObj);
        Class<?> cl = classLoader.loadClass("test.Tag");
        
        student=cl.getConstructor().newInstance();
    }
    
   // student = cl.newInstance();
    return student;
    }
    
    public  Map<String,byte[]> createByteCode() throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    Locale locale = null;
    DiagnosticListener<JavaFileObject> diagnostics = null;
    StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, locale,
            Charset.defaultCharset());
    // define where to store compiled class files - use a temporary directory
    standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT,
            Collections.singleton(Files.createTempDirectory("compile-test").toFile()));

    String filePath1 = "D:\\Raji\\CONNECT\\jar\\target\\generated-sources\\java\\test\\Tag.java";
    StringObject stringObject1 = new StringObject(
            new File("D:\\Raji\\CONNECT\\jar\\target\\generated-sources\\java\\test\\Tag.java").toURI(),
            JavaFileObject.Kind.SOURCE, readAllBytesJava7(filePath1));
    boolean TagClass = compiler.getTask(null, standardFileManager, diagnostics, Collections.emptySet(),
            Collections.emptySet(), Arrays.asList(stringObject1)).call();

    // create a file object representing the dynamic class
    FileObject tagfo = standardFileManager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, "test.Tag", Kind.CLASS);
    // these are the class bytes of the first class
    byte[] class1bytes = Files.readAllBytes(Paths.get(tagfo.toUri()));
    JavaFileObject tagjo = new SimpleJavaFileObject(URI.create("runtime:///test/Tag.class"), Kind.CLASS) {
        @Override
        public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(class1bytes);
        }
    };

    /*
     * ClassJavaFileObject tagjavaFileObject =
     * classJavaFileManager.getClassJavaFileObject(); ClassLoader classLoader = new
     * MyClassLoader(tagjavaFileObject); Class<?> cl = classLoader.loadClass("Tag");
     */
    /*
     * FileObject fo = fm.getJavaFileForInput( StandardLocation.CLASS_OUTPUT,
     * "test.Class1", Kind.CLASS); // these are the class bytes of the first class
     * byte[] class1bytes = Files.readAllBytes(Paths.get(fo.toUri()));
     */

    String filePath2 = "D:\\Raji\\CONNECT\\jar\\target\\generated-sources\\java\\test\\Category.java";

    StringObject stringObject2 = new StringObject(
            new File("D:\\Raji\\CONNECT\\jar\\target\\generated-sources\\java\\test\\Category.java").toURI(),
            JavaFileObject.Kind.SOURCE, readAllBytesJava7(filePath2));

    boolean CategoryClass = compiler.getTask(null, standardFileManager, diagnostics, Collections.emptySet(),
            Collections.emptySet(), Arrays.asList(stringObject2)).call();

    // create a file object representing the dynamic class
    FileObject categoryfo = standardFileManager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, "test.Category",
            Kind.CLASS);
    // these are the class bytes of the first class
    byte[] class2bytes = Files.readAllBytes(Paths.get(categoryfo.toUri()));
    JavaFileObject categoryjo = new SimpleJavaFileObject(URI.create("runtime:///test/Category.class"), Kind.CLASS) {
        @Override
        public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(class2bytes);
        }
    };
    /*
     * ClassJavaFileObject categoryjavaFileObject1 =
     * classJavaFileManager.getClassJavaFileObject(); ClassLoader classLoader1 = new
     * MyClassLoader(categoryjavaFileObject1); Class<?> c2 =
     * classLoader1.loadClass("Category");
     */

    // System.out.println(cl.newInstance() + "?????" + c2.newInstance());

    String filePath = "D:\\Raji\\CONNECT\\jar\\target\\generated-sources\\java\\test\\Model.java";
    StringObject stringObject = new StringObject(
            new File("D:\\Raji\\CONNECT\\jar\\target\\generated-sources\\java\\test\\Model.java").toURI(),
            JavaFileObject.Kind.SOURCE, readAllBytesJava7(filePath));

    // and a custom file manager knowing how to locate that class
    JavaFileManager myFM = new ForwardingJavaFileManager(standardFileManager) {
        /*
         * @Override public JavaFileObject getJavaFileForInput( JavaFileManager.Location
         * location, String className, Kind kind) throws IOException {
         * if(location==StandardLocation.CLASS_PATH&&className.equals("Category")) {
         * return categoryjo; } return super.getJavaFileForInput(location, className,
         * kind); }
         */

        @Override
        public boolean hasLocation(JavaFileManager.Location location) {
        return location == StandardLocation.CLASS_PATH || super.hasLocation(location);
        }

        @Override
        public Iterable list(JavaFileManager.Location location, String packageName, Set kinds, boolean recurse)
                throws IOException {

        if (location == StandardLocation.CLASS_PATH
                && (packageName.equals("test") || recurse && packageName.isEmpty())) {
            //System.out.println(location + "--1---" + packageName + "-----2-----" + kinds + "------3-----" + recurse
                    //+ "---4---" + StandardLocation.CLASS_PATH);
            List list = new ArrayList<>();
            list.add(categoryjo);
            list.add(tagjo);
            return Collections.synchronizedList(list);
        }
        return super.list(location, packageName, kinds, recurse);
        }

        @Override
        public String inferBinaryName(JavaFileManager.Location location, JavaFileObject file) {

        if (file == categoryjo) {
            //System.out.println(file + ">><<" + categoryjo);
            return "Category";
        } else if (file == tagjo) {
            //System.out.println(file + ">><<" + tagjo);
            return "Tag";
        }
        return super.inferBinaryName(location, file);
        }

    };

    boolean ModelClass = compiler.getTask(null, myFM, diagnostics, Collections.emptySet(), Collections.emptySet(),
            Arrays.asList(stringObject)).call();

    /*
     * JavaCompiler.CompilationTask task = compiler.getTask(null,
     * classJavaFileManager, null, null, null, Arrays.asList(stringObject));
     */
    //System.out.println(TagClass + "===========" + CategoryClass + "==========" + ModelClass);

    FileObject modelfo = myFM.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, "test.Model", Kind.CLASS);
    // there we have the compiled second class
    byte[] class3bytes = Files.readAllBytes(Paths.get(modelfo.toUri()));
    // create a file object representing the dynamic class
    JavaFileObject jo = new SimpleJavaFileObject(URI.create("runtime:///test/Model.class"), Kind.CLASS) {
        @Override
        public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(class3bytes);
        }
    };
   // System.out.println(class3bytes.getClass() + ">>>" + modelfo.getClass());
    Object student = null;

    // student = klass.newInstance();
                                          
    /*
     * ClassLoader classLoader1 = new MyClassLoader(categoryjo,class2bytes);
     * Class<?> c2 = classLoader1.loadClass("test.Category");
     * System.out.println(classLoader1.getParent());
     */
    // object
    Map<String,byte[]> byteObj = new HashMap<>();
    byteObj.put("test.Category", class2bytes);
    byteObj.put("test.Model", class3bytes);
    byteObj.put("test.Tag", class1bytes);
    return byteObj;
   
    }

   

    /** * Source file */
    static class StringObject extends SimpleJavaFileObject {
        private String content;

        public StringObject(URI uri, Kind kind, String content) {
        super(uri, kind);
        this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return this.content;
        }
    }

    /** * Class file (no need to save to file) */
    static class ClassJavaFileObject extends SimpleJavaFileObject {
        ByteArrayOutputStream outputStream;

        public ClassJavaFileObject(String className, Kind kind) {
        super(URI.create(className + kind.extension), kind);
        this.outputStream = new ByteArrayOutputStream();
        } // This is also achieved

        @Override
        public OutputStream openOutputStream() throws IOException {
        return this.outputStream;
        }

        public byte[] getBytes() {
        return this.outputStream.toByteArray();
        }
    }

    /** Custom ClassLoader */
    static class MyClassLoader extends ClassLoader {
        private JavaFileObject stringObject;
        private byte[] byteobj;
        private Map<String,byte[]> byteclass;
        public MyClassLoader(JavaFileObject jo,Map byteclass) {
        this.stringObject = jo;
        this.byteclass = byteclass;
        }

        
          @Override protected Class<?> findClass(String name) throws
          ClassNotFoundException { 
          
          byte[] bytes = this.byteclass.get(name); 
          return
          defineClass(name, bytes, 0, bytes.length); }
          
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
              
              if(name.startsWith("test")) {
                  Class<?> loadedClass = findLoadedClass(name);
                  if (loadedClass == null) {
                      try {
                          // find the class from given jar urls 
                          loadedClass = findClass(name);
                      } catch (ClassNotFoundException e) {
                          // Hmmm... class does not exist in the given urls.
                          // Let's try finding it in our parent classloader.
                          // this'll throw ClassNotFoundException in failure.  
                          loadedClass = super.loadClass(name);
                      }
                  }
              }

              return super.loadClass(name);
          }

    }

    private static String readAllBytesJava7(String filePath) {
    String content = "";

    try {
        content = new String(Files.readAllBytes(Paths.get(filePath)));

    } catch (IOException e) {
        e.printStackTrace();
    }

    return content;
    }

}