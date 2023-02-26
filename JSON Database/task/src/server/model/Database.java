package server.model;

import com.google.gson.*;

import java.util.*;
import java.util.logging.Level;

import static server.Main.logger;

public class Database {
    private final JsonObject jsonObjectDatabase;

    public Database() {
        jsonObjectDatabase = new JsonObject();
    }

    public synchronized JsonElement get(JsonElement keyAsJsonElement) {
        if (keyAsJsonElement.isJsonPrimitive()) {
            logger.log(Level.FINE, "## DIAG ## simple key for GET operation: " + keyAsJsonElement);
            return getBySimpleKey(keyAsJsonElement);
        } else if (keyAsJsonElement.isJsonArray()) {
            logger.log(Level.FINE, "## DIAG ## complex key for GET operation: " + keyAsJsonElement.getAsJsonArray());
            return getByComplexKey(keyAsJsonElement.getAsJsonArray());
        } else {
            throw new RuntimeException("GET request key format is invalid. Valid formats: String or JsonArray.");
        }
    }

    private JsonElement getBySimpleKey(JsonElement keyAsJsonElement) {
        JsonElement valueAsJsonElement = jsonObjectDatabase.get(keyAsJsonElement.getAsString());
        logger.log(Level.FINE, "## DIAG ## JsonElement retrieved by GET by simple key: " + valueAsJsonElement);

        return valueAsJsonElement;
    }

    private JsonElement getByComplexKey(JsonArray complexKeyAsJsonArray) {
        logger.log(Level.FINE, "## DIAG ## GET complex key size: " + complexKeyAsJsonArray.size());
        List<String> elementsOfComplexKey = new ArrayList<>();
        for (JsonElement complexKeyElement : complexKeyAsJsonArray) {
            elementsOfComplexKey.add(complexKeyElement.getAsString());
        }

        String complexKeyPrefix = elementsOfComplexKey.get(0);
        logger.log(Level.FINE, "## DIAG ## GET complex key prefix: " + complexKeyPrefix);
        JsonElement jsonElementRetrievedByKeySegment = jsonObjectDatabase.get(complexKeyPrefix);
        logger.log(Level.FINE, "## DIAG ## JsonElement retrieved by complex key prefix: " + jsonElementRetrievedByKeySegment);

        if (complexKeyAsJsonArray.size() > 1) {
            for (int i = 1; i < elementsOfComplexKey.size(); i++) {
                logger.log(Level.FINE, "## DIAG ## Located JsonElement to be set using complex key: " + jsonElementRetrievedByKeySegment);
                logger.log(Level.FINE, "## DIAG ## Performing GET with key segment: " + elementsOfComplexKey.get(elementsOfComplexKey.size() - 1));
                jsonElementRetrievedByKeySegment = jsonElementRetrievedByKeySegment.getAsJsonObject().get(elementsOfComplexKey.get(i));
            }
        }

        return jsonElementRetrievedByKeySegment;
    }

    public synchronized boolean set(JsonElement keyAsJsonElement, JsonElement valueAsJsonElement) {
        if (keyAsJsonElement.isJsonPrimitive()) {
            return setWithSimpleKey(keyAsJsonElement, valueAsJsonElement);
        } else if (keyAsJsonElement.isJsonArray()) {
            logger.log(Level.FINE, "## DIAG ## complex key for SET operation: " + keyAsJsonElement.getAsJsonArray());
            return setWithComplexKey(keyAsJsonElement.getAsJsonArray(), valueAsJsonElement);
        } else {
            throw new RuntimeException("Request key format is invalid. Valid formats: String or JsonArray.");
        }
    }

    private boolean setWithSimpleKey(JsonElement keyAsJsonElement, JsonElement valueAsJsonElement) {
        logger.log(Level.FINE, String.format("## DIAG ## simple key SET operation: %s, value: %s",
                keyAsJsonElement.getAsString(), valueAsJsonElement));
        jsonObjectDatabase.add(keyAsJsonElement.getAsString(), valueAsJsonElement);
        return true;
    }

    private boolean setWithComplexKey(JsonArray complexKeyAsJsonArray, JsonElement valueAsJsonElement) {
        if (!hasComplexKey(complexKeyAsJsonArray)) {
            return false;
        } else {
            // TODO refactor: use getByComplexKey(-1), then set
            List<String> elementsOfComplexKey = elementsOfComplexKey(complexKeyAsJsonArray);
            String complexKeyPrefix = elementsOfComplexKey.get(0);

            JsonElement jsonElementRetrievedByKeySegment = jsonObjectDatabase.get(complexKeyPrefix);
            for (int i = 1; i < elementsOfComplexKey.size() - 1; i++) { // iterate up to before the Json element level where the deletion is to be performed
                jsonElementRetrievedByKeySegment = jsonElementRetrievedByKeySegment.getAsJsonObject().get(elementsOfComplexKey.get(i));
            }
            // perform set using last element of complex key
            logger.log(Level.FINE, "## DIAG ## Located JsonElement to be set using complex key: " + jsonElementRetrievedByKeySegment);
            logger.log(Level.FINE, "## DIAG ## Performing SET with key segment: " + elementsOfComplexKey.get(elementsOfComplexKey.size() - 1));
            jsonElementRetrievedByKeySegment.getAsJsonObject()
                    .add(elementsOfComplexKey.get(elementsOfComplexKey.size() - 1), valueAsJsonElement);
        }

        return true;
    }

    public synchronized boolean delete(JsonElement keyAsJsonElement) {
        if (keyAsJsonElement.isJsonPrimitive()) {
            return deleteWithSimpleKey(keyAsJsonElement);
        } else if (keyAsJsonElement.isJsonArray()) {
            logger.log(Level.FINE, "## DIAG ## complex key for delete operation: " + keyAsJsonElement.getAsJsonArray());
            return deleteWithComplexKey(keyAsJsonElement.getAsJsonArray());
        } else {
            throw new RuntimeException("Request key format is invalid. Valid formats: String or JsonArray.");
        }
    }

    private boolean deleteWithSimpleKey(JsonElement keyAsJsonElement) {
        String key = keyAsJsonElement.getAsString();
        if (!jsonObjectDatabase.has(key)) return false;
        jsonObjectDatabase.remove(key);
        return true;
    }

    private boolean deleteWithComplexKey(JsonArray complexKeyAsJsonArray) {
        if (!hasComplexKey(complexKeyAsJsonArray)) {
            return false;
        } else {
            // TODO refactor: use getByComplexKey(-1), then delete
            List<String> elementsOfComplexKey = elementsOfComplexKey(complexKeyAsJsonArray);
            String complexKeyPrefix = elementsOfComplexKey.get(0);

            JsonElement jsonElementRetrievedByKeySegment = jsonObjectDatabase.get(complexKeyPrefix);
            for (int i = 1; i < elementsOfComplexKey.size() - 1; i++) { // iterate up to before the Json element level where the deletion is to be performed
                jsonElementRetrievedByKeySegment = jsonElementRetrievedByKeySegment.getAsJsonObject().get(elementsOfComplexKey.get(i));
            }
            // perform deletion using last element of complex key
            logger.log(Level.FINE, "## DIAG ## Located JsonElement to be deleted using complex key: " + jsonElementRetrievedByKeySegment);
            logger.log(Level.FINE, "## DIAG ## Performing DELETE with key segment: " + elementsOfComplexKey.get(elementsOfComplexKey.size() - 1));
            jsonElementRetrievedByKeySegment.getAsJsonObject().remove(elementsOfComplexKey.get(elementsOfComplexKey.size() - 1));
        }

        return true;
    }

    private List<String> elementsOfComplexKey(JsonArray complexKeyAsJsonArray) {
        List<String> elementsOfComplexKey = new ArrayList<>();
        for (JsonElement complexKeyElement : complexKeyAsJsonArray) {
            elementsOfComplexKey.add(complexKeyElement.getAsString());
        }
        return elementsOfComplexKey;
    }

    private boolean hasComplexKey(JsonArray complexKeyAsJsonArray) {
        List<String> elementsOfComplexKey = elementsOfComplexKey(complexKeyAsJsonArray);

        String complexKeyPrefix = elementsOfComplexKey.get(0);
        logger.log(Level.FINE, "## DIAG ## prefix of complex key: " + complexKeyPrefix);
        logger.log(Level.FINE, "## DIAG ## rest of complex key: " + elementsOfComplexKey.subList(1, elementsOfComplexKey.size()));

        if (jsonObjectDatabase.has(complexKeyPrefix)) {
            JsonElement jsonElementRetrievedByKeySegment = jsonObjectDatabase.get(complexKeyPrefix);
            for (int i = 1; i < elementsOfComplexKey.size(); i++) {
                if (jsonElementRetrievedByKeySegment.getAsJsonObject().has(elementsOfComplexKey.get(i))) {
                    jsonElementRetrievedByKeySegment = jsonElementRetrievedByKeySegment.getAsJsonObject().get(elementsOfComplexKey.get(i));
                } else {
                    logger.log(Level.FINE, "## DIAG ## key segment doesn't exist in DB: " + elementsOfComplexKey.get(i));
                    return false;
                }
            }
        } else {
            logger.log(Level.FINE, "## DIAG ## Prefix cannot be found in DB: " + complexKeyPrefix);
            return false;
        }
        return true;
    }

    public String getDbExport() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObjectDatabase);
    }
}
