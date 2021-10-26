package cn.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PonyZhangApplicationContext {
    private Class appConfig;

    private ConcurrentHashMap<String, Object> singleTonBeanMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();


    public PonyZhangApplicationContext(Class appConfig) {
        this.appConfig = appConfig;
        scan(appConfig);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition definition = entry.getValue();
            if (definition.getScope().equals("singleton") || definition.getScope().equals("")) {
                Object bean = createBean(definition);
                singleTonBeanMap.put(beanName, bean);
            }
        }
    }

    private void scan(Class appConfig) {
        if (appConfig.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = (ComponentScan) appConfig.getDeclaredAnnotation(ComponentScan.class);
            //拿到扫包路径
            String path = componentScan.value();
            path = path.replace(".", "/");
            //拿到类加载
            ClassLoader classLoader = this.getClass().getClassLoader();
            //获取资源路径
            URL resource = classLoader.getResource(path);
            String fName = resource.getFile();
            fName = fName.replace("%20", " ");
            File file = new File(fName);
            //获取资源路径下的所有类
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        String className = fileName.substring(fileName.indexOf("cn"), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {
                                Component annotation = clazz.getDeclaredAnnotation(Component.class);
                                String beanName = annotation.value();
                                BeanDefinition beanDefinition = new BeanDefinition();
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    String scope = clazz.getDeclaredAnnotation(Scope.class).value();
                                    beanDefinition.setScope(scope);
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinition.setClazz(clazz);
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

        } else {
            System.out.println("没有开启包扫描");
        }
    }

    public Object createBean(BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        Object instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields){
                if(field.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(instance,bean);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("prototype".equals(beanDefinition.getScope())) {
                return createBean(beanDefinition);
            } else {
                return singleTonBeanMap.get(beanName);
            }
        } else {
            throw new NullPointerException();
        }
    }

    public static void main(String[] args) {
        File file = new File("F:\\java learning\\handwirte\\spring\\target\\classes\\cn\\ponyzhang\\service");
        if (file.isDirectory()) {
            System.out.println(1);
        }
    }
}
