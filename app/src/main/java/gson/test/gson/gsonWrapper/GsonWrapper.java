package gson.test.gson.gsonWrapper;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhangyu
 */

public final class GsonWrapper {


    public static final Gson wrapper(Gson gson){

        replaceReflectiveTypeAdapterFactory(gson);

        return gson;
    }

    private static boolean replaceReflectiveTypeAdapterFactory(Gson gson) {
        try {

            Field field = Gson.class.getDeclaredField("factories");
            boolean oldAccessible = field.isAccessible();
            field.setAccessible(true);

            List<TypeAdapterFactory> gsonFactories = (List<TypeAdapterFactory>) field.get(gson);

            if (gsonFactories == null) return true;

            gsonFactories = Collections.unmodifiableList(newGsonTypeAdapterFactories(gsonFactories));

            field.set(gson,gsonFactories);
            field.setAccessible(oldAccessible);

        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    @NonNull
    private static List<TypeAdapterFactory> newGsonTypeAdapterFactories(List<TypeAdapterFactory> factories) {

        ArrayList<TypeAdapterFactory> newFactories = new ArrayList<>();

        newFactories.addAll(factories);

        TypeAdapterFactory oldFactory = null;

        Iterator<TypeAdapterFactory> iterable = newFactories.iterator();
        while (iterable.hasNext()){
            oldFactory = iterable.next();
            if (oldFactory instanceof ReflectiveTypeAdapterFactory)break;
        }

        if (oldFactory == null) return newFactories;

        int index = newFactories.indexOf(oldFactory);

        final TypeAdapterFactory newFactory  = newFactories.remove(index);

        newFactories.add(index , new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                TypeAdapter<T> adapter =  newFactory.create(gson,type);
                Class<? super T> raw = type.getRawType();

                adapter = new TypeAdapterExt<>((ReflectiveTypeAdapterFactory.Adapter)adapter,getDeclaredFields(type,raw));
                return adapter;
            }
        });
        return newFactories;
    }

    private static List<Field> getDeclaredFields(TypeToken<?> type, Class<?> raw) {
        List<Field> result = new ArrayList<>();
        if (raw.isInterface()) {
            return result;
        }
        while (raw != Object.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                result.add(field);
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    private static class TypeAdapterExt<T> extends TypeAdapter<T>{

        ReflectiveTypeAdapterFactory.Adapter<T> realAdapter;
        List<Field> declaredFields = new ArrayList<>();
        
        private TypeAdapterExt(ReflectiveTypeAdapterFactory.Adapter<T> adapter,List<Field> fields){
            this.realAdapter = adapter;
            this.declaredFields.clear();
            if (fields != null) this.declaredFields.addAll(fields);
        }
        @Override
        public void write(JsonWriter out, T value) throws IOException{
//            checkObjOptionalField(value);
            this.realAdapter.write(out,value);
        }

        @Override
        public T read(JsonReader in) throws IOException{
            T result = realAdapter.read(in);

            checkObjOptionalField(result);
            return result;
        }

        private void checkObjOptionalField(T result) throws NotOptionalFieldException{
            StringBuilder notOptionalCollection = new StringBuilder();
            Optional optional;
            Object fieldValue;

            boolean findNotOptional = false;

            for (Field field: declaredFields) {

                optional = field.getAnnotation(Optional.class);
                if (optional == null || optional.optional())continue;

                try{
                    field.setAccessible(true);
                    fieldValue = field.get(result);
                }catch (Throwable e){
                    continue;
                }

                if (null == fieldValue){
                    if (!findNotOptional)findNotOptional = true;
                    if (notOptionalCollection.length() > 0) notOptionalCollection.append(",");
                    notOptionalCollection.append(field.getName());
                }
            }

            if (findNotOptional){

                notOptionalCollection.insert(0,"[");
                notOptionalCollection.append("]");

                Log.e("TAG","findError:" + notOptionalCollection.toString());
                throw new NotOptionalFieldException("not Optional field : " + notOptionalCollection.toString());
            }
        }
    }

}
