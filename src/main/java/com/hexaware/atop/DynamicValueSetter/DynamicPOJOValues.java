package com.hexaware.atop.DynamicValueSetter;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexaware.atop.util.AtopConstants;
import com.sun.codemodel.JCodeModel;

public class DynamicPOJOValues {
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, Exception {
    // TODO Auto-generated method stub
    System.out.println("argument received: " + args[0]);
    String FILE_PATH = args[0];
     FileInputStream fis = new FileInputStream(new File(FILE_PATH+"\\ApiData.xlsx"));
    String jsonFilePath = FILE_PATH;
    
   // FileInputStream fis = new FileInputStream(new File(AtopConstants.FILE_PATH+"ApiData.xlsx"));
    //String jsonFilePath = AtopConstants.FILE_PATH;

    File folder = new File(jsonFilePath);
    XSSFWorkbook wb = new XSSFWorkbook(fis);

    File[] listOfFiles = folder.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
        // TODO Auto-generated method stub
        if (pathname.getName().endsWith(".json"))
            return true;
        else
            return false;

        }
    });

    for (File jsonFile : listOfFiles) {
  
        System.out.println(jsonFile.getAbsolutePath() + "====" + jsonFile.getName());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> staticMap = mapper.readValue(new File(jsonFile.getAbsolutePath()), Map.class);

        convertJsonToJavaClass(new File(jsonFile.getAbsolutePath()).toURL(), new File(AtopConstants.PACKAGE_NAME), "test",
                "Model" );
        Thread.sleep(5000);
        //DynamicPOJO pojos = new DynamicPOJO();
       
        //Class class_ = Class.forName("Model" + counter);


        // creating Workbook instance that refers to .xlsx file

        XSSFSheet s = wb.getSheet(jsonFile.getName().replace(".json", ""));
        // XSSFSheet s = wb.getSheetAt(0); // creating a Sheet object to retrieve object
        XSSFRow headerRow = (XSSFRow) s.getRow(0);
        int headerColumnCount = headerRow.getLastCellNum();
        int rowCount = s.getLastRowNum();
        String[] methodNames = new String[headerColumnCount];

        Class<?>[] fieldTypes = new Class<?>[headerColumnCount];
        for (int columnIndex = 0; columnIndex < headerColumnCount; columnIndex++) {// traverse the header column
            String data = headerRow.getCell(columnIndex).toString();
            methodNames[columnIndex] = data;
        }

        for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {
            // Get row object
            XSSFRow row = s.getRow(rowIndex);
            if (row != null) {
                Object obj = null;
                // instantiate an object of the generic class an object
                try {
                    DynamicPOJO dyanmicpojo = new DynamicPOJO();
                    //obj = class_.getDeclaredConstructor().newInstance();
                     obj= dyanmicpojo.createStudent("test.Model");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                for (int columnIndex = 0; columnIndex < headerColumnCount; columnIndex++) {
                    if (row.getCell(columnIndex) != null) {
                        String methodName = methodNames[columnIndex];
                        String fieldname = methodName;
                        String data = new DataFormatter().formatCellValue(row.getCell(columnIndex));

                        switch (row.getCell(columnIndex).getCellType()) {
                        case NUMERIC:
                            boolean intNumber = data.matches("[+-]?[0-9]+");
                            if (intNumber) {
                                setFieldValue(obj, fieldname, Integer.parseInt(data));

                            } else {
                                setFieldValue(obj, fieldname, Double.parseDouble(data));
                            }
                            break;
                        case BOOLEAN:
                            // String data = row.getCell(columnIndex).toString();
                            setFieldValue(obj, fieldname, data);
                            break;
                        case STRING:
                            // String data = row.getCell(columnIndex).toString();
                            if (data.equals("true") || data.equals("false")) {
                                setFieldValue(obj, fieldname, new Boolean(data));
                            } else {
                                setFieldValue(obj, fieldname, data);
                            }

                            break;

                        }


                    }

                }
                Field[] fields = obj.getClass().getDeclaredFields();
                boolean availableVar = false;
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getName() != "objectMapper")
                        for (String staticKey : staticMap.keySet()) {
                            if (field.getName().contains(staticKey) && field.get(obj) == null) {
                                Method setter = obj.getClass().getDeclaredMethod(fieldToSetterName(field.getName()),
                                        staticMap.get(staticKey).getClass());
                                setter.invoke(obj, staticMap.get(staticKey));

                            }
                        }
                }

                System.out.println("Values are set" + obj);
            }

        }
        // wb.close();
    }
    wb.close();
    fis.close();

    }
// Set values in POJO

    private static void setFieldValue(Object object, String fieldName, Object fieldValue) throws Exception {
    if (fieldName.contains(".") && fieldName.contains("[")) {

        int firstDotLocation = fieldName.indexOf('[');
        int paramLocation = fieldName.indexOf('.');
        String childFieldName = fieldName.substring(0, firstDotLocation);
        String getIndex = fieldName.substring(firstDotLocation + 1, firstDotLocation + 2);
        Method getter = object.getClass().getDeclaredMethod(fieldToGetterName(childFieldName));
        Field field = object.getClass().getDeclaredField(childFieldName);
        Object childFieldInstance = getter.invoke(object);
        Class<?> fieldTypeParameterType = null;
        List<Object> children = new ArrayList<Object>();
        Class<?> type = getter.getReturnType();
        // invoking no argument constructor
        ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
        // get it's first type parameter
        fieldTypeParameterType = (Class<?>) fieldGenericType.getActualTypeArguments()[0];
        
        DynamicPOJO dyanmicpojo = new DynamicPOJO();
        Object aobj = dyanmicpojo.createStudent(fieldTypeParameterType.getName());
        Class a = aobj.getClass();
        if (childFieldInstance != null) {
            if (childFieldInstance instanceof List<?>) {
                if (((List) childFieldInstance).size() > Integer.parseInt(getIndex)) {
                    int index = Integer.parseInt(getIndex);
                    children.addAll((List) childFieldInstance);
                    children.remove(index);
                    childFieldInstance = ((List) childFieldInstance).get(index);
                } else {
                    children.addAll((List) childFieldInstance);
                    childFieldInstance = a.newInstance();
                }

            }
        } else if (childFieldInstance == null) {
            childFieldInstance = a.newInstance();
        }

        Object childObject = valueSetter(childFieldInstance, fieldName.substring(paramLocation + 1), fieldValue);

        children.add(childObject);
        Method Objsetter = object.getClass().getDeclaredMethod(fieldToSetterName(childFieldName),
                getter.getReturnType());
        Objsetter.invoke(object, children);
        // setFieldValue(childFieldInstance, fieldName.substring(paramLocation + 1),
        // fieldValue);
    } else if (fieldName.contains("[")) {
        int firstDotLocation = fieldName.indexOf('[');
        String childFieldName = fieldName.substring(0, firstDotLocation);
        String getIndex = fieldName.substring(firstDotLocation + 1, firstDotLocation + 2);
        Method getter = object.getClass().getDeclaredMethod(fieldToGetterName(childFieldName));
        Field field = object.getClass().getDeclaredField(childFieldName);
        Object childFieldInstance = getter.invoke(object);
        Class<?> fieldTypeParameterType = null;
        List<Object> children = new ArrayList<Object>();
        Class<?> type = getter.getReturnType();
        // invoking no argument constructor
        ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
        // get it's first type parameter
        fieldTypeParameterType = (Class<?>) fieldGenericType.getActualTypeArguments()[0];
        System.out.println(fieldTypeParameterType.getName());
        Class a = Class.forName(fieldTypeParameterType.getName());

        if (childFieldInstance != null) {
            if (childFieldInstance instanceof List<?>) {
                if (((List) childFieldInstance).size() > Integer.parseInt(getIndex)) {
                    int index = Integer.parseInt(getIndex);
                    children.addAll((List) childFieldInstance);
                    childFieldInstance = ((List) childFieldInstance).get(index);
                } else {
                    children.addAll((List) childFieldInstance);
                    childFieldInstance = a.newInstance();
                }

            }
        } else if (childFieldInstance == null) {
            childFieldInstance = a.newInstance();
        }
        childFieldInstance = fieldValue;

        // Object childObject = valueSetter(childFieldInstance, childFieldName,
        // fieldValue);

        children.add(childFieldInstance);
        Method Objsetter = object.getClass().getDeclaredMethod(fieldToSetterName(childFieldName),
                getter.getReturnType());
        Objsetter.invoke(object, children);
    } else if (fieldName.contains(".")) {
        int firstDotLocation = fieldName.indexOf('.');
        String childFieldName = fieldName.substring(0, firstDotLocation);
        Method getter = object.getClass().getDeclaredMethod(fieldToGetterName(childFieldName));
        Object childFieldInstance = getter.invoke(object);
        if (childFieldInstance == null) {
            Class<?> type = getter.getReturnType();
            // invoking no argument constructor
            childFieldInstance = type.getConstructor().newInstance();
            Method setter = object.getClass().getDeclaredMethod(fieldToSetterName(childFieldName), type);
            setter.invoke(object, childFieldInstance);
        }
        setFieldValue(childFieldInstance, fieldName.substring(firstDotLocation + 1), fieldValue);
    }

    else {
        
        Method setter = object.getClass().getDeclaredMethod(fieldToSetterName(fieldName), fieldValue.getClass());
        //Method setter = object.getClass().getMethod(fieldToSetterName(fieldName), fieldValue.getClass());
        setter.invoke(object, fieldValue);
    }
    }

    private static String fieldToGetterName(String fieldName) {
    return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private static String fieldToSetterName(String fieldName) {
    return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private static Object valueSetter(Object object, String fieldName, Object fieldValue) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method setter = object.getClass().getDeclaredMethod(fieldToSetterName(fieldName), fieldValue.getClass());
    setter.invoke(object, fieldValue);
    return object;
    }

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    static {
        WRAPPER_TYPE_MAP = new HashMap<Class<?>, Class<?>>(16);
        WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        WRAPPER_TYPE_MAP.put(Character.class, char.class);
        WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_TYPE_MAP.put(Double.class, double.class);
        WRAPPER_TYPE_MAP.put(Float.class, float.class);
        WRAPPER_TYPE_MAP.put(Long.class, long.class);
        WRAPPER_TYPE_MAP.put(Short.class, short.class);
        WRAPPER_TYPE_MAP.put(Void.class, void.class);
        WRAPPER_TYPE_MAP.put(String.class, String.class);
    }

    public static void convertJsonToJavaClass(URL inputJsonUrl, File outputJavaClassDirectory, String packageName,
            String javaClassName) throws IOException {
    JCodeModel jcodeModel = new JCodeModel();

    GenerationConfig config = new DefaultGenerationConfig() {
        @Override
        public boolean isGenerateBuilders() {
        return false;
        }

        @Override
        public SourceType getSourceType() {
        return SourceType.JSON;
        }

        @Override
        public boolean isIncludeAdditionalProperties() {
        return false;
        }

        @Override
        public boolean isIncludeHashcodeAndEquals() {
        return false;
        }

    };

    SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()),
            new SchemaGenerator());
    mapper.generate(jcodeModel, javaClassName, packageName, inputJsonUrl);
    File target = new File("D:/Raji/CONNECT/jar/target/generated-sources/java");
    if(!target.exists()) {
        target.mkdirs();
    }
    else {
        deleteDir(target);
        target.mkdirs();
    }
    /*
     * if (!target.mkdirs()) { throw new IOException("could not create directory");
     * }
     */
    jcodeModel.build(target);
   // jcodeModel.build(outputJavaClassDirectory);

    }
    public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
        String[] children = dir.list();
        for (int i=0; i<children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success) {
                return false;
            }
        }
    }
    return dir.delete();
}
}
