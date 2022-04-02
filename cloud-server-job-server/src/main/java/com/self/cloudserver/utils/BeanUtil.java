package com.self.cloudserver.utils;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BeanUtil {

    public static void copyNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullField(source));
    }

    private static String[] getNullField(Object target) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(target);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
        Set<String> notNullFieldSet = new HashSet<>();
        if (propertyDescriptors.length > 0) {
            for (PropertyDescriptor p : propertyDescriptors) {
                String name = p.getName();
                Object value = beanWrapper.getPropertyValue(name);
                if (Objects.isNull(value) || StringUtils.isBlank(value.toString())) {
                    notNullFieldSet.add(name);
                }
            }
        }

        String[] notNullField = new String[notNullFieldSet.size()];
        return notNullFieldSet.toArray(notNullField);
    }

    public static <E, T> List<E> copyList(List<T> srcList, Class<E> clazz) {
        if (CollectionUtils.isEmpty(srcList)) {
            return Collections.emptyList();
        }
        return srcList.stream().map(src -> copyProperties(src, clazz)).collect(Collectors.toList());
    }

    public static <T> T copyProperties(Object src, Class<T> tClass) {
        try {
            if (src == null) {
                return null;
            }
            T newInstance = tClass.newInstance();
            BeanUtils.copyProperties(src, newInstance);
            return newInstance;
        } catch (Exception e) {
            return null;
        }
    }


    public static Map<String,Object> beanToMapNotNull(Object o){
        Map<String,Object> result = new HashMap<>();
        Class<?> tClass = o.getClass();
        Field[] fields = tClass.getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            try {
                String name = f.getName();
                String key = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
                Object o1 = f.get(o);
                if(o1==null){
                    continue;
                }else if(o1 instanceof String &&StringUtils.isEmpty((String)o1)){
                    continue;
                }else{
                    result.put(key,o1);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Map<String, Object> beanToMap(Object o){
        Map<String, Object> result = new HashMap<>();
        Class<?> tClass = o.getClass();
        Field[] fields = tClass.getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            try {
                String name = f.getName();
                result.put(name, f.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
