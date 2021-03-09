package openfoodfacts.github.scrachx.openfood.models.entities;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.util.Base64.DEFAULT;

class MapOfStringsToStringConverter implements PropertyConverter<Map<String, String>, String> {
    private static final String LOG_TAG;

    @Override
    @NonNull
    public Map<String, String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return new HashMap<>();
        }
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(databaseValue, DEFAULT));
            ObjectInputStream objectInputStream = new ObjectInputStream(bis);
            objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(LOG_TAG, "Cannot serialize map to database.", e);
        }
        return new HashMap<>();
    }

    @Override
    @NonNull
    public String convertToDatabaseValue(Map<String, String> entity) {
        HashMap<String, String> map = entity != null ? new HashMap<>(entity) : new HashMap<>();

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
            objectOutputStream.writeObject(map);
            objectOutputStream.flush();
            return Base64.encodeToString(bos.toByteArray(), DEFAULT);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot serialize map to database.", e);
        }
        return "";
    }

    static {
        LOG_TAG = MapOfStringsToStringConverter.class.getSimpleName();
    }
}
